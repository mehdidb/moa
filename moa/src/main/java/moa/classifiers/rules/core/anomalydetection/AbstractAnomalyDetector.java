package moa.classifiers.rules.core.anomalydetection;

import moa.options.AbstractOptionHandler;
import org.apache.samoa.instances.MultiLabelInstance;


public abstract class AbstractAnomalyDetector extends AbstractOptionHandler implements AnomalyDetector {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public abstract boolean updateAndCheckAnomalyDetection(MultiLabelInstance instance);

	@Override
	public AnomalyDetector copy() {
		return (AnomalyDetector) super.copy();
	}

}
