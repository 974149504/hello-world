package sip.core.provider;

/**
 * @author Beowulf
 */
public interface Context
{
    void post(Runnable runnable);

    /**
     * 定时执行
     *
     * @param runnable 任务
     * @param interval 定时
     */
    void run(Runnable runnable, int interval);

    /**
     * 延迟执行
     *
     * @param runnable 任务
     * @param delay    延迟
     */
    void runDelay(Runnable runnable, int delay);

    /**
     * 取消执行任务
     *
     * @param runnable 任务
     */
    void remove(Runnable runnable);
}
