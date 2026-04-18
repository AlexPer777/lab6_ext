package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import commands.LocalDateTimeAdapter;
import model.Car;
import model.HumanBeing;
import util.DefaultRecursionController;
import util.HumanBeingParser;
import response.Response;
import response.ResponseStatus;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CollectionManager {
    private final Vector<HumanBeing> collection = new Vector<>();
    private final LocalDateTime initTime = LocalDateTime.now();
    private final Map<UUID, HumanBeing> humansById = new HashMap<>();
    private final InputValidator inputValidator;
    private final HumanBeingParser humanBeingParser;
    private final DefaultRecursionController recursionController;
    private final String collectionFilePath;

    public CollectionManager(Scanner scanner, String collectionFilePath) {
        this.inputValidator = new InputValidator(scanner);
        this.humanBeingParser = new HumanBeingParser();
        this.recursionController = new DefaultRecursionController();
        this.collectionFilePath = collectionFilePath;
    }

    public List<HumanBeing> show() {
        return collection.stream()
                .sorted(Comparator.comparing(HumanBeing::getName))
                .collect(Collectors.toList());
    }

    public String clear(){
        collection.clear();
        humansById.clear();
        return "Clear command executed successfully";
    }

    public String sort() {
        List<HumanBeing> sorted = collection.stream()
                .sorted()
                .collect(Collectors.toList());
        collection.clear();
        collection.addAll(sorted);
        return "Коллекция отсортирована.";
    }

    public String info(){
        return "Information about collections\n" +
                "Type: Human Being\n" +
                "Date initialization: " + initTime + "\n" +
                "Quantity elements: " + collection.size() + "\n";
    }

    public String help(){
        return """
help : вывести справку по доступным командам
info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении
add {element} : добавить новый элемент в коллекцию
update id {element} : обновить значение элемента коллекции, id которого равен заданному
remove_by_id id : удалить элемент из коллекции по его id
clear : очистить коллекцию
save : сохранить коллекцию в файл
execute_script file_name : считать и исполнить скрипт из указанного файла. Для add, update id и remove_greater в скрипте поля элемента можно передавать следующими строками.
remove_at index : удалить элемент, находящийся в заданной позиции коллекции (index)
remove_greater {element} : удалить из коллекции все элементы, превышающие заданный
sort : отсортировать коллекцию в естественном порядке
count_greater_than_car car : вывести количество элементов, значение поля car которых больше заданного
filter_contains_name name : вывести элементы, значение поля name которых содержит заданную подстроку
filter_starts_with_name name : вывести элементы, значение поля name которых начинается с заданной подстроки
""";
    }

    public String save(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        try (FileWriter writer = new FileWriter(collectionFilePath)) {
            gson.toJson(collection, writer);
            return "Коллекция сохранена";
        } catch (IOException e) {
            return "Ошибка сохранения файла";
        }
    }

    public String add(HumanBeing human) {
        human.setId(generateUniqueId());
        collection.add(human);
        humansById.put(human.getId(), human);
        return "Element added successfully!";
    }

    public String countGreaterThanCar(Car inputCar) {
        long count = collection.stream()
                .map(HumanBeing::getCar)
                .filter(Objects::nonNull)
                .filter(car -> car.compareTo(inputCar) > 0)
                .count();
        return "Количество элементов: " + count;
    }

    public Response executeScript(String fileName, Reader reader, ResponsePrinter responsePrinter) {
        if (recursionController.checkRecursion(fileName)) {
            return new Response("Recursion detected! Script already running: " + fileName, ResponseStatus.ERROR, null);
        }
        try {
            recursionController.pushScript(fileName);
            ListIterator<String> scriptIterator = readScriptLines(fileName).listIterator();
            return executeScriptLines(scriptIterator, reader, responsePrinter, fileName);
        } catch (IOException e) {
            return new Response("File not found: " + fileName, ResponseStatus.ERROR, null);
        } finally {
            recursionController.popScript(fileName);
        }
    }

    private List<String> readScriptLines(String fileName) throws IOException {
        try (var lines = Files.lines(Path.of(fileName))) {
            return lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    private Response executeScriptLines(ListIterator<String> scriptIterator, Reader reader,
                                        ResponsePrinter responsePrinter, String fileName) {
        if (!scriptIterator.hasNext()) {
            return new Response("Скрипт выполнен: " + fileName, ResponseStatus.SUCCESS, null);
        }
        String line = prepareScriptCommand(scriptIterator.next(), scriptIterator);
        if (line == null) {
            return new Response("Ошибка чтения скрипта: " + fileName, ResponseStatus.ERROR, null);
        }
        System.out.println(">> " + line);
        responsePrinter.print(reader.getLine(line));
        return executeScriptLines(scriptIterator, reader, responsePrinter, fileName);
    }

    private String prepareScriptCommand(String line, ListIterator<String> scriptIterator) {
        if ("add".equals(line)) {
            String humanData = readScriptHumanBeingData(scriptIterator, "add");
            return humanData == null ? null : "add {" + humanData + "}";
        }
        if ("remove_greater".equals(line)) {
            String humanData = readScriptHumanBeingData(scriptIterator, "remove_greater");
            return humanData == null ? null : "remove_greater {" + humanData + "}";
        }
        if (line.startsWith("update ") && !line.contains("{")) {
            String[] parts = line.split("\\s+", 2);
            if (parts.length < 2 || parts[1].isBlank()) {
                System.out.println("Ошибка: для update в скрипте нужно указать id отдельной строкой команды");
                return null;
            }
            String humanData = readScriptHumanBeingData(scriptIterator, "update");
            return humanData == null ? null : "update " + parts[1].trim() + " {" + humanData + "}";
        }
        return line;
    }

    private String readScriptHumanBeingData(ListIterator<String> scriptIterator, String commandName) {
        List<String> values = IntStream.range(0, 9)
                .mapToObj(i -> readNextScriptValue(scriptIterator, commandName))
                .collect(Collectors.toCollection(ArrayList::new));
        if (values.stream().anyMatch(Objects::isNull)) {
            return null;
        }
        String carName = values.get(8);
        if (!"null".equalsIgnoreCase(carName)) {
            String carCool = readNextScriptValue(scriptIterator, commandName);
            if (carCool == null) {
                return null;
            }
            values.add(carCool);
        }

        return String.join(" ", values);
    }

    private String readNextScriptValue(ListIterator<String> scriptIterator, String commandName) {
        if (scriptIterator.hasNext()) {
            return scriptIterator.next();
        }
        System.out.println("Ошибка: в скрипте недостаточно данных для команды " + commandName);
        return null;
    }

    public String update(UUID id, HumanBeing updatedHuman) {
        HumanBeing human = collection.stream()
                .filter(currentHuman -> currentHuman.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (human == null) {
            return "Элемент с таким id не найден";
        }
        human.setName(updatedHuman.getName());
        human.setCoordinates(updatedHuman.getCoordinates());
        human.setRealHero(updatedHuman.isRealHero());
        human.setHasToothpick(updatedHuman.getHasToothpick());
        human.setImpactSpeed(updatedHuman.getImpactSpeed());
        human.setWeaponType(updatedHuman.getWeaponType());
        human.setMood(updatedHuman.getMood());
        human.setCar(updatedHuman.getCar());
        return "Элемент успешно обновлён";
    }

    public String removeGreater(HumanBeing example) {
        Set<UUID> idsToRemove = collection.stream()
                .filter(human -> human.compareTo(example) > 0)
                .map(HumanBeing::getId)
                .collect(Collectors.toSet());
        int removed = idsToRemove.size();
        collection.removeIf(human -> idsToRemove.contains(human.getId()));
        idsToRemove.forEach(humansById::remove);
        return "Удалено элементов: " + removed;
    }

    public List<HumanBeing> filterContainsName(String substring) {
        return collection.stream()
                .filter(human -> human.getName().contains(substring))
                .sorted(Comparator.comparing(HumanBeing::getName))
                .collect(Collectors.toList());
    }

    public List<HumanBeing> filterStartsWithName(String prefix) {
        return collection.stream()
                .filter(human -> human.getName().startsWith(prefix))
                .sorted(Comparator.comparing(HumanBeing::getName))
                .collect(Collectors.toList());
    }

    public String removeAt(long parameter) {
        int index = (int) parameter;
        if (index < 0 || index >= collection.size()) {
            return "Индекс вне диапазона коллекции";
        }
        HumanBeing humanBeing = collection.stream()
                .skip(index)
                .findFirst()
                .orElse(null);
        if (humanBeing == null) {
            return "Индекс вне диапазона коллекции";
        }
        collection.remove(index);
        humansById.remove(humanBeing.getId());
        return "Элемент на позиции " + index + " " + humanBeing.getName() + " удалён";
    }

    public String removeById(UUID id) {
        HumanBeing humanBeing = collection.stream()
                .filter(human -> human.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (humanBeing == null) {
            return "Элемент с таким ID не найден";
        }
        collection.remove(humanBeing);
        humansById.remove(id);
        return "Элемент с id " + id + " успешно удалён";
    }

    public HumanBeing createHumanBeingForAdd() {
        return inputValidator.readHumanBeing();
    }

    public HumanBeing createHumanBeingTemplate() {
        return inputValidator.readHumanBeing();
    }

    public HumanBeing parseHumanBeingForAdd(String raw) {
        return humanBeingParser.parse(raw);
    }

    public HumanBeing parseHumanBeingTemplate(String raw) {
        return humanBeingParser.parse(raw);
    }

    public Car readCar() {
        return inputValidator.readCar();
    }

    public Car parseCar(String raw) {
        try {
            String[] parts = raw.trim().split("\\s+");
            if (parts.length != 2) {
                return null;
            }
            return new Car(parts[0], Boolean.parseBoolean(parts[1]));
        } catch (Exception e) {
            return null;
        }
    }

    public void addLoadedHumanBeing(HumanBeing humanBeing) {
        boolean idAlreadyExists = collection.stream()
                .anyMatch(human -> human.getId().equals(humanBeing.getId()));
        if (idAlreadyExists) {
            humanBeing.setId(generateUniqueId());
        }
        collection.add(humanBeing);
        humansById.put(humanBeing.getId(), humanBeing);
    }

    private UUID generateUniqueId() {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (containsId(id));
        return id;
    }

    private boolean containsId(UUID id) {
        return collection.stream()
                .anyMatch(human -> human.getId().equals(id));
    }
}
