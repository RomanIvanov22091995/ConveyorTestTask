package org.example.conveyor;

import lombok.Data;

@Data
public class ConveyorNode {
    private Integer value;
    private final int position;
    private final NodeType type;

    public ConveyorNode(int position, NodeType type) {
        this.position = position;
        this.type = type;
        this.value = null;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public boolean hasValue() {
        return value != null;
    }


    public enum NodeType {
        QUEUE_A_ONLY,
        QUEUE_B_ONLY,
        INTERSECTION
    }
}
