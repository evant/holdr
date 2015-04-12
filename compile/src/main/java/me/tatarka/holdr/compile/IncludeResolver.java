package me.tatarka.holdr.compile;

import me.tatarka.holdr.model.Include;
import me.tatarka.holdr.model.Layout;

/**
 * Created by evan on 1/17/15.
 */
public interface IncludeResolver {
    Layout resolveInclude(Include include);
}
