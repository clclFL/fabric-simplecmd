package net.pineclone.simplecmd.utils;

import net.minecraft.util.math.MathHelper;

import static net.pineclone.simplecmd.utils.Ticker.*;

public class TickerTask {

    public static DelayTask delay(Runnable doRun) {
        return new DelayTask(doRun);
    }

    public static DelayTask delay(Runnable doRun, long delay) {
        return new DelayTask(doRun, delay);
    }

    public static PollingTask polling(Runnable doRun, long duration) {
        return new PollingTask(doRun, duration);
    }

    public static PollingTask polling(Runnable doRun, long duration, int tps) {
        return new PollingTask(doRun, duration, tps);
    }

    public static class PollingTask implements TickableTask {
        private boolean isCanceled;
        private boolean isPaused;
        private boolean hasInit;
        private long when;
        private long duration;
        private int tps;
        private final Runnable doRun;

        private PollingTask(Runnable doRun, long duration) {
            this(doRun, duration, 20);
        }

        private PollingTask(Runnable doRun, long duration, int tps) {
            this.doRun = doRun;
            this.duration = duration;
            this.tps = MathHelper.clamp(20 / tps, 1, 20);
        }

        @Override
        public void exec(long ticks) {
            if (!hasInit) {
                when = ticks;
                hasInit = true;
            }

            if (isPaused || isCanceled) {
                return;
            }

            if (ticks > when + duration) {
                cancel();
            }

            if (ticks % tps == 0) {
                doRun.run();
            }
        }

        @Override
        public void pause() {
            if (isCanceled) return;
            isPaused = true;
            hasInit = false;
            duration = duration - (server.getTicks() - when);
        }

        @Override
        public void resume() {
            if (isCanceled) return;
            isPaused = false;
        }

        @Override
        public void cancel() {
            isCanceled = true;
            TASKS.remove(this);
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }


        public void setTps(int tps) {
            this.tps = MathHelper.clamp(20 / tps, 1, 20);
        }
    }

    public static class DelayTask implements TickableTask {
        private boolean isCanceled;
        private boolean isPaused;
        private boolean hasInit;
        private long delay;
        private long when;
        private Runnable doRun;


        private DelayTask(Runnable doRun) {
            this(doRun, 0);
        }

        private DelayTask(Runnable doRun, long delay) {
            this.doRun = doRun;
            this.delay = delay;
        }

        public void exec(long ticks) {
            if (!hasInit) {
                this.when = ticks;
                hasInit = true;
            }

            if (isPaused || isCanceled) {
                return;
            }

            if (ticks >= when + delay) {
                doRun.run();
                cancel();
            }
        }

        public void pause() {
            if (isCanceled) return;
            delay = delay - (server.getTicks() - when);
            hasInit = false;
            isPaused = true;
        }

        public void resume() {
            if (isCanceled) return;
            isPaused = false;
        }

        private void setDelay(long delay) {
            this.delay = delay;
        }

        private void setTask(Runnable task) {
            this.doRun = task;
        }

        public void cancel() {
            this.isCanceled = true;
            TASKS.remove(this);
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        public long getRemainingTicks() {
            if (isCanceled) return 0;
            if (isPaused) return delay;
            return delay - (server.getTicks() - when);
        }
    }

    public interface TickableTask {
        void exec(long ticks);

        void pause();

        void resume();

        void cancel();

        boolean isCanceled();

        default TickableTask onCanceled(Runnable then) {
            return new TaskProxy(this) {
                @Override
                public void onCancel() {
                    then.run();
                }
            };
        }
    }

    private static class TaskProxy implements TickableTask {
        private boolean isPaused;
        private final TickableTask task;
        private boolean doneCancelHook;

        public TaskProxy(TickableTask task) {
            this.task = task;
        }

        @Override
        public void exec(long ticks) {
            if (isPaused || task.isCanceled()) {
                cancel();
                if (!doneCancelHook) {
                    onCancel();
                    doneCancelHook = true;
                }
                return;
            }
            task.exec(ticks);
            onExec();
        }

        @Override
        public void pause() {
            isPaused = true;
            task.pause();
            onPause();
        }

        @Override
        public void resume() {
            isPaused = false;
            task.resume();
            onResume();
        }

        @Override
        public void cancel() {
            task.cancel();
            TASKS.remove(this);
        }

        @Override
        public boolean isCanceled() {
            return task.isCanceled();
        }

        public void onCancel() {
        }

        public void onResume() {
        }

        public void onPause() {
        }

        public void onExec() {
        }
    }
}
