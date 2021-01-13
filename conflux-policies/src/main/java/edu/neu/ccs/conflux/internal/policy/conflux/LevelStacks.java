package edu.neu.ccs.conflux.internal.policy.conflux;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.ArrayList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;

/**
 * Maps between instability levels and stacks of taint tags.
 *
 * @param <E> the type of the labels used in the stack's taint tags
 */
final class LevelStacks<E> {
    private final List<Node<E>> stacks;

    /**
     * Creates a new empty mapping.
     */
    LevelStacks() {
        stacks = new ArrayList<>();
    }

    /**
     * Create a copy of the specified other mapping.
     * The stacks are shallow copied.
     *
     * @param other the mapping to be copied
     */
    LevelStacks(LevelStacks<E> other) {
        this.stacks = new ArrayList<>(other.stacks.size());
        for (int i = 0; i < other.stacks.size(); i++) {
            stacks.add(other.get(i));
        }
    }

    private boolean containsKey(int level) {
        return level < stacks.size() && stacks.get(level) != null;
    }

    private Node<E> get(int level) {
        return stacks.get(level);
    }

    private void set(int level, Node<E> value) {
        while (level >= stacks.size()) {
            stacks.add(null);
        }
        stacks.set(level, value);
    }

    /**
     * Removes all of the mappings from this map.
     */
    void clear() {
        stacks.clear();
    }

    /**
     * Removes the mapping for the specified level from this map if present.
     *
     * @param level level whose mapping is to be removed from this map
     */
    void remove(int level) {
        if (containsKey(level)) {
            set(level, null);
        }
    }

    Taint<E> getTagUnderLevel(int level) {
        Taint<E> tag = Taint.emptyTaint();
        for (int key = 0; key < stacks.size() && key <= level; key++) {
            if (containsKey(key)) {
                tag = get(key).getUnion(tag);
            }
        }
        return tag;
    }

    /**
     * Pops the top element off of the stack mapped to the specified level if present.
     *
     * @param level level whose stack will have an element popped from it
     */
    void pop(int level) {
        if (containsKey(level)) {
            set(level, get(level).next);
        }
    }

    /**
     * Sets the taint tag of the top element of the stack mapped to the specified level to be the union
     * of its current taint tag and the specified tag.
     *
     * @param level level whose stack whose top is to be unioned into
     * @param tag   the taint tag to be unioned into the top of the specified level's stack
     */
    void union(int level, Taint<E> tag) {
        if (containsKey(level)) {
            get(level).setUnion(tag);
        } else {
            set(level, new Node<>(tag, null));
        }
    }

    /**
     * Pushes the union of the current taint tag of the top of the stack mapped to the specified level (if present)
     * with the specified taint tag onto the stack mapped to the specified level.
     *
     * @param level level whose stack will have an element pushed onto it
     * @param tag   the taint tag to be pushed onto the specified level's stack
     */
    void push(int level, Taint<E> tag) {
        if (containsKey(level)) {
            Node<E> top = get(level);
            set(level, new Node<>(top.getUnion(tag), top));
        } else {
            set(level, new Node<>(tag, null));
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (int key = 0; key < stacks.size(); key++) {
            if (containsKey(key)) {
                if (!first) {
                    builder.append(", ");
                }
                first = false;
                builder.append(key).append(" = ").append(get(key));
            }
        }
        return builder.append("}").toString();
    }

    private static final class Node<E> {
        private final Node<E> next;
        private Taint<E> tag;

        private Node(Taint<E> tag, Node<E> next) {
            this.tag = tag;
            this.next = next;
        }

        private void setUnion(Taint<E> tag) {
            this.tag = Taint.combineTags(this.tag, tag);
        }

        private Taint<E> getUnion(Taint<E> tag) {
            return Taint.combineTags(this.tag, tag);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[");
            for (Node<E> cur = this; cur != null; cur = cur.next) {
                builder.append(cur.tag);
                if (cur.next != null) {
                    builder.append(", ");
                }
            }
            return builder.append("]").toString();
        }
    }
}
