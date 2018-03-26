package com.itextpdf.svg.renderers.factories;

import com.itextpdf.styledxmlparser.node.IElementNode;
import com.itextpdf.svg.exceptions.SvgLogMessageConstant;
import com.itextpdf.svg.exceptions.SvgProcessingException;
import com.itextpdf.svg.renderers.ISvgNodeRenderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of {@link ISvgNodeRendererFactory} that will be
 * used by default by the entry points defined by this project.
 */
public class DefaultSvgNodeRendererFactory implements ISvgNodeRendererFactory {

    private Map<String, Class<? extends ISvgNodeRenderer>> rendererMap = new HashMap<>();
    private Collection<String> ignoredTags = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSvgNodeRendererFactory.class);

    /**
     * Default constructor which uses the default {@link ISvgNodeRendererMapper}
     * implementation.
     */
    public DefaultSvgNodeRendererFactory() {
        this(new DefaultSvgNodeRendererMapper());
    }

    /**
     * Constructor which allows injecting a custom
     * {@link ISvgNodeRendererMapper} implementation.
     *
     * @param mapper the custom mapper implementation - if null, then we fall
     * back to the {@link DefaultSvgNodeRendererMapper}
     */
    public DefaultSvgNodeRendererFactory(ISvgNodeRendererMapper mapper) {
        if (mapper != null) {
            rendererMap.putAll(mapper.getMapping());
            ignoredTags.addAll(mapper.getIgnoredTags());
        } else {
            ISvgNodeRendererMapper defaultMapper = new DefaultSvgNodeRendererMapper();
            rendererMap.putAll(defaultMapper.getMapping());
            ignoredTags.addAll(defaultMapper.getIgnoredTags());
        }
    }

    @Override
    public ISvgNodeRenderer createSvgNodeRendererForTag(IElementNode tag, ISvgNodeRenderer parent) {
        ISvgNodeRenderer result;

        if (tag == null) {
            throw new SvgProcessingException(SvgLogMessageConstant.TAGPARAMETERNULL);
        }

        try {
            Class<? extends ISvgNodeRenderer> clazz = rendererMap.get(tag.name());

            if (clazz == null) {
                throw new SvgProcessingException(SvgLogMessageConstant.UNMAPPEDTAG).setMessageParams(tag.name());
            }

            result = (ISvgNodeRenderer) rendererMap.get(tag.name()).newInstance();
        } catch (ReflectiveOperationException ex) {
            LOGGER.error(DefaultSvgNodeRendererFactory.class.getName(), ex);
            throw new SvgProcessingException(SvgLogMessageConstant.COULDNOTINSTANTIATE, ex).setMessageParams(tag.name());
        }

        if (parent != null) {
            result.setParent(parent);
        }

        return result;
    }

    @Override
    public boolean isTagIgnored(IElementNode tag){
        return ignoredTags.contains(tag.name());
    }
}
