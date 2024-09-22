package logic;

import exceptions.*;
import tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTasksManager(File file) {
        this.file = file;
    }

    public static void main(String[] args) {
        FileBackedTasksManager manager = new FileBackedTasksManager(new File("data/data.csv"));
        FileBackedTasksManager manager1;

        //Дополнительное задание. Реализуем пользовательский сценарий.
        Task task1 = new Task(10, "Задача №1", "Описание задачи 1");
        manager.taskCreator(task1);
        Task task2 = new Task(20, "Задача №2", "Описание задачи 2");
        manager.taskCreator(task2);

        Epic epic1 = new Epic(1000, "Эпик №1", "С тремя подзадачами"); //1000
        manager.epicCreator(epic1);

        Subtask subtask1 = new Subtask("Подзадача № 1", "Описание подзадачи 1", epic1); // 1
        manager.subtaskCreator(subtask1);
        Subtask subtask2 = new Subtask("Подзадача № 2", "Описание подзадачи 2", epic1); // 2
        manager.subtaskCreator(subtask2);
        Subtask subtask3 = new Subtask("Подзадача № 3", "Описание подзадачи 3", epic1); // 3
        manager.subtaskCreator(subtask3);

        Epic epic2 = new Epic(2000, "Эпик №2", "Без подзадач"); //1000
        manager.epicCreator(epic2);

        System.out.println("Всего создано задач - " + (manager.getTasks().size() + manager.getSubtasks().size() + manager.getEpics().size()));

        //Запрос некоторых задач, чтобы заполнилась история просмотра.
        System.out.println("\n----------Обращение к задачам (10,20,100,1,2,3,200):");
        manager.getTaskById(10);
        manager.getTaskById(20);
        manager.getEpicById(1000);
        manager.getSubtaskById(1);
        manager.getSubtaskById(2);
        manager.getSubtaskById(3);
        manager.getEpicById(2000);

        //Просмотр истории обращения к задачам
        System.out.println("\nСписок обращений к задачам:");
        for (Task taskFor : manager.history()) {
            System.out.println(taskFor);
        }

        System.out.println("\n----------Создание второго менеджера на основе файла первого экземпляра.");

        // Создание нового FileBackedTasksManager менеджера из этого же файла.
        manager1 = loadFromFile(Paths.get("data/data.csv").toFile()); //toPath

        // Вывод списка задач
        System.out.println("Всего создано задач - " + (manager1.getTasks().size() + manager1.getSubtasks().size() + manager1.getEpics().size()));
        System.out.println("\nСписок обращений к задачам после загрузки из файла:");
        for (Task taskFor : manager1.history()) {
            System.out.println("#" + taskFor.getId() + " - " + taskFor.getTitle() + " " + taskFor.getDescription() + " (" + taskFor.getStatus() + ")");
        }

    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();
            for (Task task : taskHashMap.values()) {
                writer.write(task.toString());
                writer.newLine();
            }
            for (Epic epic : epicHashMap.values()) {
                writer.write(epic.toString());
                writer.newLine();
                for (Subtask subtask : subtaskHashMap.values()) {
                    writer.write(subtask.toString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи файла.");
        }
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager manager = new FileBackedTasksManager(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            reader.readLine(); // пропустить заголовок
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                Task task = manager.fromString(line);
                if (task.getTaskType() == TaskType.TASK) {
                    manager.taskHashMap.put(task.getId(), task);
                } else if (task.getTaskType() == TaskType.EPIC) {
                    manager.epicHashMap.put(task.getId(), (Epic) task);
                } else if (task.getTaskType() == TaskType.SUBTASK) {
                    manager.subtaskHashMap.put(task.getId(), (Subtask) task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла.");
        }
        return manager;
    }

    @Override
    public void taskCreator(Task task) {
        super.taskCreator(task);
        save();
    }

    @Override
    public void epicCreator(Epic epic) {
        super.epicCreator(epic);
        save();
    }

    @Override
    public void subtaskCreator(Subtask subtask) {
        super.subtaskCreator(subtask);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }


    @Override
    public List<Task> history() {
        return super.history();
    }

    private Task fromString(String value) {
        String[] line = value.split(",");
        int id = Integer.parseInt(line[0]);
        String taskType = line[1];
        String name = line[2];
        TaskStatus status = TaskStatus.valueOf(line[3]);
        String description = line[4];

        switch (taskType) {
            case "TASK":
                Task task = new Task(name, description);
                task.setStatus(status);
                task.setId(id);
                return task;
            case "EPIC":
                Epic epic = new Epic(name, description);
                epic.setId(id);
                return epic;
            case "SUBTASK":
                int epicId = Integer.parseInt(line[5]);
                Subtask subtask = new Subtask(name, description, epicHashMap.get(epicId));
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                return null;
        }
    }

}