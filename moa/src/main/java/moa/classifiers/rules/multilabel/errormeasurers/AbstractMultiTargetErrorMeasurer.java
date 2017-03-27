package moa.classifiers.rules.multilabel.errormeasurers;

import org.apache.samoa.instances.MultiLabelInstance;
import org.apache.samoa.instances.MultiLabelPrediction;
import org.apache.samoa.instances.Prediction;

public abstract class AbstractMultiTargetErrorMeasurer extends AbstractMultiLabelErrorMeasurer implements MultiLabelErrorMeasurer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void addPrediction(Prediction prediction, MultiLabelInstance inst){
		int numOutputs=inst.numberOutputTargets();
		Prediction trueClass= new MultiLabelPrediction(numOutputs);
		for (int i=0; i<numOutputs; i++){
			trueClass.setVotes(i, new double[]{inst.valueOutputAttribute(i)});
		}
		addPrediction(prediction, trueClass, inst.weight());
	}

}
