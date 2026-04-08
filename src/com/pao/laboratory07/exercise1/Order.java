package com.pao.laboratory07.exercise1;

import com.pao.laboratory07.exercise1.exceptions.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class Order {
    private OrderState currentState;
    private final Deque<OrderState> history = new ArrayDeque<>();

    public Order(OrderState initialState) {
        this.currentState = initialState;
    }

    public void nextState() throws OrderIsAlreadyFinalException {
        if (isFinalState()) {
            throw new OrderIsAlreadyFinalException();
        }

        history.push(currentState);
        currentState = switch (currentState) {
            case PLACED -> OrderState.PROCESSED;
            case PROCESSED -> OrderState.SHIPPED;
            case SHIPPED -> OrderState.DELIVERED;
            default -> currentState;
        };
        System.out.println("Order state updated to: " + currentState);
    }

    public void cancel() throws CannotCancelFinalOrderException {
        if (isFinalState()) {
            throw new CannotCancelFinalOrderException();
        }

        history.push(currentState);
        currentState = OrderState.CANCELED;
        System.out.println("Order has been canceled.");
    }

    public void undoState() throws CannotRevertInitialOrderStateException {
        if (history.isEmpty()) {
            throw new CannotRevertInitialOrderStateException();
        }

        currentState = history.pop();
        System.out.println("Order state reverted to: " + currentState);
    }

    private boolean isFinalState() {
        return currentState == OrderState.DELIVERED || currentState == OrderState.CANCELED;
    }
}