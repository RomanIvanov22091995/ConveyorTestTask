package org.example.conveyor;

import lombok.Getter;
import org.example.conveyor.exception.NonPrimeNumberException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Conveyor {
    private final List<ConveyorNode> queueA;
    private final List<ConveyorNode> queueB;
    private final Map<Integer, ConveyorNode> intersectionNodes;

    public Conveyor(ConveyorConfig config) {
        this.queueA = new ArrayList<>();
        this.queueB = new ArrayList<>();
        this.intersectionNodes = new HashMap<>();
        initializeQueues(config);
    }

    private void initializeQueues(ConveyorConfig config) {
        Map<Integer, Integer> intersectionMapA = new HashMap<>();
        Map<Integer, Integer> intersectionMapB = new HashMap<>();

        config.getIntersections().forEach(intersection -> {
            intersectionMapA.put(intersection.getPositionInA(), intersection.getPositionInB());
            intersectionMapB.put(intersection.getPositionInB(), intersection.getPositionInA());
        });

        for (int i = 0; i < config.getQueueALength(); i++) {
            ConveyorNode node = intersectionMapA.containsKey(i)
                ? createIntersectionNode(i, intersectionMapA.get(i))
                : new ConveyorNode(i, ConveyorNode.NodeType.QUEUE_A_ONLY);
            queueA.add(node);
        }

        for (int i = 0; i < config.getQueueBLength(); i++) {
            ConveyorNode node = intersectionMapB.containsKey(i)
                ? intersectionNodes.get(getIntersectionKey(intersectionMapB.get(i), i))
                : new ConveyorNode(i, ConveyorNode.NodeType.QUEUE_B_ONLY);
            queueB.add(node);
        }
    }

    private ConveyorNode createIntersectionNode(int posA, int posB) {
        ConveyorNode node = new ConveyorNode(posA, ConveyorNode.NodeType.INTERSECTION);
        intersectionNodes.put(getIntersectionKey(posA, posB), node);
        return node;
    }

    private int getIntersectionKey(int posA, int posB) {
        return posA * 10000 + posB;
    }

    public int pushA(int value) {
        validatePrime(value);
        Integer output = shiftQueue(queueA, value);
        return output != null ? output : 0;
    }

    public int pushB(int value) {
        validatePrime(value);
        Integer output = shiftQueue(queueB, value);
        return output != null ? output : 0;
    }

    private void validatePrime(int number) {
        if (!isPrime(number)) {
            throw new NonPrimeNumberException(number);
        }
    }

    private Integer shiftQueue(List<ConveyorNode> queue, int newValue) {
        ConveyorNode lastNode = queue.get(queue.size() - 1);
        Integer outputValue = lastNode.hasValue() ? lastNode.getValue() : null;

        for (int i = queue.size() - 1; i > 0; i--) {
            queue.get(i).setValue(queue.get(i - 1).getValue());
        }

        queue.get(0).setValue(newValue);
        return outputValue;
    }

    private boolean isPrime(int number) {
        if (number < 2) {
            return false;
        }
        return switch (number) {
            case 2 -> true;
            default -> number % 2 != 0 && checkOddDivisors(number);
        };
    }

    private boolean checkOddDivisors(int number) {
        for (int i = 3; i * i <= number; i += 2) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }

    public List<Integer> getQueueAState() {
        return getQueueState(queueA);
    }

    public List<Integer> getQueueBState() {
        return getQueueState(queueB);
    }

    private List<Integer> getQueueState(List<ConveyorNode> queue) {
        return queue.stream()
            .map(ConveyorNode::getValue)
            .toList();
    }

    public int getQueueALength() {
        return queueA.size();
    }

    public int getQueueBLength() {
        return queueB.size();
    }
}
