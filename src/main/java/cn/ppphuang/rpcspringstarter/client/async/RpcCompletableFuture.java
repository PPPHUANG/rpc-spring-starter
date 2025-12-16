package cn.ppphuang.rpcspringstarter.client.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 专用于 RPC 异步调用的 CompletableFuture 包装类。
 * <p>
 * 目的：防止在 RPC-Client-CallBackExecutor 线程中执行阻塞逻辑。
 * <p>
 * ⚠️ <b>警示：</b>
 * RPC 回调直接运行在 IO 线程中。
 * - ❌ <b>严禁 IO 操作</b>：禁止 RPC、DB、文件读写、Thread.sleep 等。会导致 IO 线程耗尽，系统瘫痪。
 * - ✅ <b>允许 CPU 操作</b>：允许 DTO 转 VO、简单的 List 处理等轻量级内存操作。
 * <p>
 * 🛠 <b>开发指引：</b>
 * 1. 如果包含 IO 操作 ⏩ 请务必使用 {@link #thenApplyAsync(Function, Executor)} 并指定线程池。
 * 2. 如果仅是对象组装 ⏩ 请使用 {@link #thenApplyNonBlocking(Function)} 来消除警告。
 */
public class RpcCompletableFuture<T> extends CompletableFuture<T> {

    /**
     * 将标准的 CompletableFuture 包装为 RpcCompletableFuture。
     * 这样可以确保链式调用的每一步都受到 RpcCompletableFuture 的安全检查。
     */
    private static <U> RpcCompletableFuture<U> wrap(CompletableFuture<U> original) {
        RpcCompletableFuture<U> wrapper = new RpcCompletableFuture<>();
        original.whenComplete((res, ex) -> {
            if (ex != null) {
                wrapper.completeExceptionally(ex);
            } else {
                wrapper.complete(res);
            }
        });
        return wrapper;
    }

    // ==================== 1. 有返回值的转换 (Function) ====================

    /**
     * ⛔️ <b>已禁用提示</b>
     * <p>
     * 请确认回调逻辑是否包含 IO 操作：
     * - 有 IO 操作：请改用 {@link #thenApplyAsync(Function, Executor)}。
     * - 无 IO 操作（仅对象组装）：请改用 {@link #thenApplyNonBlocking(Function)}。
     */
    @Override
    @Deprecated
    public <U> RpcCompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return wrap(super.thenApply(fn));
    }

    @Override
    public <U> RpcCompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        return wrap(super.thenApplyAsync(fn, executor));
    }

    /**
     * ✅ <b>无阻塞安全调用</b>
     * <p>
     * 仅当确信回调逻辑为<b>纯内存操作</b>（如对象组装、数据转换）且<b>执行极快</b>时使用。
     */
    public <U> RpcCompletableFuture<U> thenApplyNonBlocking(Function<? super T, ? extends U> fn) {
        return wrap(super.thenApply(fn));
    }

    // ==================== 2. 纯消费 (Consumer) ====================

    /**
     * ⛔️ <b>已禁用提示</b>
     * <p>
     * 请确认回调逻辑是否包含 IO 操作：
     * - 有 IO 操作：请改用 {@link #thenAcceptAsync(Consumer, Executor)}。
     * - 无 IO 操作（仅对象组装）：请改用 {@link #thenAcceptNonBlocking(Consumer)}。
     */
    @Override
    @Deprecated
    public RpcCompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return wrap(super.thenAccept(action));
    }

    @Override
    public RpcCompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return wrap(super.thenAcceptAsync(action, executor));
    }

    /**
     * ✅ <b>无阻塞安全调用</b>
     * <p>
     * 仅当确信回调逻辑为<b>纯内存操作</b>（如 setter 赋值）且<b>执行极快</b>时使用。
     */
    public RpcCompletableFuture<Void> thenAcceptNonBlocking(Consumer<? super T> action) {
        return wrap(super.thenAccept(action));
    }

    /**
     * ⛔️ <b>已禁用提示</b>
     * <p>
     * 请确认回调逻辑是否包含 IO 操作：
     * - 有 IO 操作：请改用 {@link #thenRunAsync(Runnable, Executor)}。
     * - 无 IO 操作（仅对象组装）：请改用 {@link #thenRunNonBlocking(Runnable)}。
     */
    @Override
    @Deprecated
    public RpcCompletableFuture<Void> thenRun(Runnable action) {
        return wrap(super.thenRun(action));
    }

    @Override
    public RpcCompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        return wrap(super.thenRunAsync(action, executor));
    }

    /**
     * ✅ <b>无阻塞安全调用</b>
     * <p>
     * 仅当确信回调逻辑为<b>纯内存操作</b>且<b>执行极快</b>时使用。
     */
    public RpcCompletableFuture<Void> thenRunNonBlocking(Runnable action) {
        return wrap(super.thenRun(action));
    }

    // ==================== 3. 结果处理 (Handle/WhenComplete) ====================

    /**
     * ⛔️ <b>已禁用提示</b>
     * <p>
     * 请检查是否包含 IO 操作。纯内存操作请使用 {@link #handleNonBlocking(BiFunction)}。
     */
    @Override
    @Deprecated
    public <U> RpcCompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return wrap(super.handle(fn));
    }

    @Override
    public <U> RpcCompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return wrap(super.handleAsync(fn, executor));
    }

    /**
     * ✅ <b>无阻塞安全调用</b>
     * <p>
     * 仅当确信回调逻辑为<b>纯内存操作</b>且<b>执行极快</b>时使用。
     */
    public <U> RpcCompletableFuture<U> handleNonBlocking(BiFunction<? super T, Throwable, ? extends U> fn) {
        return wrap(super.handle(fn));
    }

    /**
     * ⛔️ <b>已禁用提示</b>
     * <p>
     * 请检查是否包含 IO 操作。纯内存操作请使用 {@link #whenCompleteNonBlocking(BiConsumer)}。
     */
    @Override
    @Deprecated
    public RpcCompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return wrap(super.whenComplete(action));
    }

    @Override
    public RpcCompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return wrap(super.whenCompleteAsync(action, executor));
    }

    /**
     * ✅ <b>无阻塞安全调用</b>
     * <p>
     * 仅当确信回调逻辑为<b>纯内存操作</b>且<b>执行极快</b>时使用。
     */
    public RpcCompletableFuture<T> whenCompleteNonBlocking(BiConsumer<? super T, ? super Throwable> action) {
        return wrap(super.whenComplete(action));
    }
}
