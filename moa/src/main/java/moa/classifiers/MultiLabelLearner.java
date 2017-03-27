package moa.classifiers;

import org.apache.samoa.instances.MultiLabelInstance;
import org.apache.samoa.instances.Prediction;

public interface MultiLabelLearner extends Classifier{
	
	public void trainOnInstanceImpl(MultiLabelInstance instance);
	
	public Prediction getPredictionForInstance(MultiLabelInstance instance);

}
