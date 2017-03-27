package moa.classifiers;

import moa.core.Example;
import org.apache.samoa.instances.Instance;
import org.apache.samoa.instances.MultiLabelInstance;
import org.apache.samoa.instances.Prediction;

public abstract class AbstractMultiLabelLearner extends AbstractClassifier implements MultiLabelLearner {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    abstract public void trainOnInstanceImpl(MultiLabelInstance instance);

    @Override
    public void trainOnInstanceImpl(Instance instance) {
        trainOnInstanceImpl((MultiLabelInstance) instance);
    }

    @Override
    public Prediction getPredictionForInstance(Example<Instance> example) {
        return getPredictionForInstance(example.getData());
    }

    @Override
    public Prediction getPredictionForInstance(Instance inst) {
        return getPredictionForInstance((MultiLabelInstance) inst);
    }

    abstract public Prediction getPredictionForInstance(MultiLabelInstance inst);

    @Override
    public double[] getVotesForInstance(Instance inst) {
        Prediction pred = getPredictionForInstance(inst);
        if (pred != null) {
            return pred.getVotes();
        } else {
            return new double[]{0}; //for compatibility with single target code
        }
    }

}
