package me.tatarka.holdr.compile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import me.tatarka.holdr.compile.util.Objects;

/**
 * Created by evan on 8/30/14.
 */
public class ParsedLayout {
    @Nullable
    public final String superclass;
    @NotNull
    public final Collection<Ref> refs;

    public ParsedLayout(Collection<Ref> refs) {
        this(null, refs);
    }

    public ParsedLayout(@Nullable String superclass, @NotNull Collection<Ref> refs) {
        this.superclass = superclass;
        this.refs = refs;
    }
    
    @NotNull
    public String getSuperclass() {
        return superclass != null ? superclass : HoldrGenerator.HOLDR_SUPERCLASS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedLayout that = (ParsedLayout) o;

        return refs.equals(that.refs) && getSuperclass().equals(that.getSuperclass());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(superclass, refs);
    }
}
