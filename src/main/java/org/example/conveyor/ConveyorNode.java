package org.example.conveyor;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ConveyorNode {
    private Integer value;
    private final Integer position;
    private final NodeType type;

    public boolean hasValue() {
        return value != null;
    }


    public enum NodeType {
        QUEUE_A_ONLY,
        QUEUE_B_ONLY,
        INTERSECTION
    }
}
