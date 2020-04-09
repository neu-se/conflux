package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressBasicBlock;

import java.util.function.Function;

public class TacConverter implements Function<ThreeAddressBasicBlock, AnnotatedBasicBlock> {

    private final PropagationTransformer transformer;

    public TacConverter(PropagationTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public AnnotatedBasicBlock apply(ThreeAddressBasicBlock vertex) {
        return vertex.createSSABasicBlock(transformer);

    }
}
