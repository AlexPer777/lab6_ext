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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class CollectionManager {
    private final Vector<HumanBeing> collection = new Vector<>();
    private final LocalDateTime initTime = LocalDateTime.now();
    private final Map<Integer, HumanBeing> humansById = new HashMap<>();
    private final InputValidator inputValidator;
    private final HumanBeingParser humanBeingParser;
    private final DefaultRecursionController recursionController;
    private final String collectionFilePath;
    private int nextId = 1;

    public CollectionManager(Scanner scanner, String collectionFilePath) {
        this.inputValidator = new InputValidator(scanner);
        this.humanBeingParser = new HumanBeingParser();
        this.recursionController = new DefaultRecursionController();
        this.collectionFilePath = collectionFilePath;
    }

    public List<HumanBeing> show() {
        return new ArrayList<>(collection);
    }

    public String clear(){
        collection.clear();
        humansById.clear();
        return "Clear command executed successfully";
    }

    public String sort() {
        Collections.sort(collection);
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
exit : завершить программу (без сохранения в файл)
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
        collection.add(human);
        humansById.put(human.getId(), human);
        nextId = Math.max(nextId, human.getId() + 1);
        return "Element added successfully!";
    }

    public String countGreaterThanCar(Car inputCar) {
        int count = 0;
        for (HumanBeing human : collection) {
            Car car = human.getCar();
            if (car != null && car.compareTo(inputCar) > 0) {
                count++;
            }
        }
        return "Количество элементов: " + count;
    }

    public Response executeScript(String fileName, Reader reader, ResponsePrinter responsePrinter) {
        if (recursionController.checkRecursion(fileName)) {
            return new Response("Recursion detected! Script already running: " + fileName, ResponseStatus.ERROR, null);
        }
        try (Scanner fileScanner = new Scanner(new FileInputStream(fileName))) {
            recursionController.pushScript(fileName);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                line = prepareScriptCommand(line, fileScanner);
                if (line == null) {
                    return new Response("Ошибка чтения скрипта: " + fileName, ResponseStatus.ERROR, null);
                }
                System.out.println(">> " + line);
                responsePrinter.print(reader.getLine(line));
            }
        } catch (FileNotFoundException e) {
            return new Response("File not found: " + fileName, ResponseStatus.ERROR, null);
        } finally {
            recursionController.popScript(fileName);
        }
        return new Response("Скрипт выполнен: " + fileName, ResponseStatus.SUCCESS, null);
    }
    private String prepareScriptCommand(String line, Scanner fileScanner) {
        if ("add".equals(line)) {
            String humanData = readScriptHumanBeingData(fileScanner, "add");
            return humanData == null ? null : "add {" + humanData + "}";
        }
        if ("remove_greater".equals(line)) {
            String humanData = readScriptHumanBeingData(fileScanner, "remove_greater");
            return humanData == null ? null : "remove_greater {" + humanData + "}";
        }
        if (line.startsWith("update ") && !line.contains("{")) {
            String[] parts = line.split("\\s+", 2);
            if (parts.length < 2 || parts[1].isBlank()) {
                System.out.println("Ошибка: для update в скрипте нужно указать id отдельной строкой команды");
                return null;
            }
            String humanData = readScriptHumanBeingData(fileScanner, "update");
            return humanData == null ? null : "update " + parts[1].trim() + " {" + humanData + "}";
        }
        return line;
    }
    private String readScriptHumanBeingData(Scanner fileScanner, String commandName) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            String value = readNextScriptValue(fileScanner, commandName);
            if (value == null) {
                return null;
            }
            values.add(value);
        }
        String carName = values.get(8);
        if (!"null".equalsIgnoreCase(carName)) {
            String carCool = readNextScriptValue(fileScanner, commandName);
            if (carCool == null) {
                return null;
            }
            values.add(carCool);
        }

        return String.join(" ", values);
    }
    private String readNextScriptValue(Scanner fileScanner, String commandName) {
        while (fileScanner.hasNextLine()) {
            String value = fileScanner.nextLine().trim();
            if (value.isEmpty()) {
                continue;
            }
            return value;
        }
        System.out.println("Ошибка: в скрипте недостаточно данных для команды " + commandName);
        return null;
    }

    public String update(long id, HumanBeing updatedHuman) {
        if (!humansById.containsKey((int) id)) {
            return "Элемент с таким id не найден";
        }
        HumanBeing human = humansById.get((int) id);
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
        Iterator<HumanBeing> iterator = collection.iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            HumanBeing human = iterator.next();
            if (human.compareTo(example) > 0) {
                iterator.remove();
                humansById.remove(human.getId());
                removed++;
            }
        }
        return "Удалено элементов: " + removed;
    }

    public List<HumanBeing> filterContainsName(String substring) {
        List<HumanBeing> result = new ArrayList<>();
        for (HumanBeing human : collection) {
            if (human.getName().contains(substring)) {
                result.add(human);
            }
        }
        return result;
    }

    public List<HumanBeing> filterStartsWithName(String prefix) {
        List<HumanBeing> result = new ArrayList<>();
        for (HumanBeing human : collection) {
            if (human.getName().startsWith(prefix)) {
                result.add(human);
            }
        }
        return result;
    }

    public String removeAt(long parameter) {
        int index = (int) parameter;
        if (index < 0 || index >= collection.size()) {
            return "Индекс вне диапазона коллекции";
        }
        HumanBeing humanBeing = collection.get(index);
        collection.remove(index);
        humansById.remove(humanBeing.getId());
        return "Элемент на позиции " + index + " удалён";
    }

    public String removeById(long parameter) {
        int id = (int) parameter;
        if (!humansById.containsKey(id)) {
            return "Элемент с таким ID не найден";
        }
        HumanBeing humanBeing = humansById.get(id);
        collection.remove(humanBeing);
        humansById.remove(id);
        return "Элемент с id " + id + " успешно удалён";
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public HumanBeing createHumanBeingForAdd() {
        return inputValidator.readHumanBeing(nextId);
    }

    public HumanBeing createHumanBeingTemplate() {
        return inputValidator.readHumanBeing(0);
    }

    public HumanBeing parseHumanBeingForAdd(String raw) {
        return humanBeingParser.parse(raw, nextId);
    }

    public HumanBeing parseHumanBeingTemplate(String raw) {
        return humanBeingParser.parse(raw, 0);
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
        collection.add(humanBeing);
        humansById.put(humanBeing.getId(), humanBeing);
    }
}
