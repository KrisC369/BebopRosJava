package taskexecutor.interruptors;

import keyboard.Key;
import services.rossubscribers.RosKeyObserver;
import taskexecutor.EmergencyNotifier;
import taskexecutor.Task;
import taskexecutor.TaskExecutor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Hoang Tung Dinh
 */
public final class KeyboardEmergency implements EmergencyNotifier, RosKeyObserver {
    private final Task task;
    private final Collection<TaskExecutor> taskExecutors = new ArrayList<>();

    private KeyboardEmergency(Task task) {
        this.task = task;
    }

    /**
     * Creates a keyboard emergency notifier. It will notify all the subscribed {@link TaskExecutor} when key "x" is
     * pressed.
     *
     * @param task the emergency task to be executed when key "x" is pressed
     * @return a keyboard emergency notifier
     */
    public static KeyboardEmergency create(Task task) {
        return new KeyboardEmergency(task);
    }

    @Override
    public void registerTaskExecutor(TaskExecutor taskExecutor) {
        taskExecutors.add(taskExecutor);
    }

    @Override
    public void removeTaskExecutor(TaskExecutor taskExecutor) {
        taskExecutors.remove(taskExecutor);
    }

    @Override
    public void update(Key key) {
        if (key.getCode() == Key.KEY_x) {
            for (final TaskExecutor taskExecutor : taskExecutors) {
                taskExecutor.submitTask(task);
            }
        }
    }
}
