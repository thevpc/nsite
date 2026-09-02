package net.thevpc.nsite.javadoc;

import net.thevpc.nuts.lib.md.MdElement;
import java.util.List;

public interface JDDoc {

    String getTag(String tag);

    List<String> getTags(String tag);

    MdElement getDescription();

    List<JDBlockTag> getBlockTags();

    String rawText();
}
