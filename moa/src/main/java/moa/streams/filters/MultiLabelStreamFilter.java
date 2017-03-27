package moa.streams.filters;

import org.apache.samoa.instances.Instance;

import moa.core.Example;
import moa.streams.ExampleStream;
import moa.streams.MultiTargetInstanceStream;

public interface MultiLabelStreamFilter extends MultiTargetInstanceStream {

    /**
     * Sets the input stream to the filter
     *
     * @param stream the input stream to the filter
     */
    public void setInputStream(ExampleStream<Example<Instance>> stream);
}
