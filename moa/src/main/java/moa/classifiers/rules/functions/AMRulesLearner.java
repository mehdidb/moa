package moa.classifiers.rules.functions;

import moa.core.Example;
import moa.learners.Learner;

import org.apache.samoa.instances.Instance;

public interface AMRulesLearner extends Learner<Example<Instance>>{
	
	public double getCurrentError();

}
