package moa.classifiers.rules.multilabel.meta;

import moa.classifiers.AbstractMultiLabelLearner;
import moa.classifiers.MultiTargetRegressor;
import moa.classifiers.rules.multilabel.AMRulesMultiLabelLearner;
import moa.classifiers.rules.multilabel.core.voting.ErrorWeightedVoteMultiLabel;
import moa.classifiers.rules.multilabel.core.voting.UniformWeightedVoteMultiLabel;
import moa.classifiers.rules.multilabel.errormeasurers.AbstractMultiTargetErrorMeasurer;
import moa.classifiers.rules.multilabel.errormeasurers.MultiLabelErrorMeasurer;
import moa.core.Measurement;
import moa.core.MiscUtils;
import moa.options.ClassOption;

import com.github.javacliparser.FlagOption;
import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.MultiChoiceOption;
import org.apache.samoa.instances.Instance;
import org.apache.samoa.instances.MultiLabelInstance;
import org.apache.samoa.instances.Prediction;

public class MultiLabelRandomAMRules extends AbstractMultiLabelLearner
implements MultiTargetRegressor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IntOption VerbosityOption = new IntOption(
			"verbosity",
			'v',
			"Output Verbosity Control Level. 1 (Less) to 2 (More)",
			1, 1, 2);


	public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l', "Classifier to train.", AMRulesMultiLabelLearner.class, "AMRulesMultiTargetRegressor"); 

	public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's',
			"The number of models in the bag.", 10, 1, Integer.MAX_VALUE);

	public FloatOption numAttributesPercentageOption = new FloatOption("numAttributesPercentage", 'n',
			"The number of attributes to use per model.", 63.2, 0, 100); 

	public FlagOption useBaggingOption = new FlagOption("useBagging", 'p',
			"Use Bagging.");

	public ClassOption votingFunctionOption = new ClassOption("votingFunction",
			'V', "Voting Function.", 
			ErrorWeightedVoteMultiLabel.class,
			UniformWeightedVoteMultiLabel.class.getName());

	public MultiChoiceOption votingTypeOption = new MultiChoiceOption(
			"votingTypeOption", 'C', "Select whether the base learner error is computed as the overall error or only the error of the rules that cover the example.", new String[]{
					"Overall (Static)","Only rules covered (Dynamic)"}, new String[]{
					"Overall","Covered"}, 0);


	public IntOption randomSeedOption = new IntOption("randomSeed", 'r',
			"Seed for random behaviour of the classifier.", 1);
	protected AMRulesMultiLabelLearner [] ensemble;

	protected MultiLabelErrorMeasurer [] errorMeasurer;

	public ClassOption errorMeasurerOption = new ClassOption("errorMeasurer", 'e',
			"Measure of error for deciding which learner should predict.", AbstractMultiTargetErrorMeasurer.class, "MeanAbsoluteDeviationMT") ;

	/*protected double[] sumError;
	protected double[] nError;*/


	protected boolean isRegression;

	@Override
	public void resetLearningImpl() {
		this.classifierRandom.setSeed(this.randomSeedOption.getValue());
		int n=this.ensembleSizeOption.getValue();
		this.ensemble= new AMRulesMultiLabelLearner[n];
		this.errorMeasurer= new MultiLabelErrorMeasurer[n];


		AMRulesMultiLabelLearner baseLearner = (AMRulesMultiLabelLearner) getPreparedClassOption(this.baseLearnerOption);
		MultiLabelErrorMeasurer measurer=(MultiLabelErrorMeasurer) getPreparedClassOption(this.errorMeasurerOption);
		baseLearner.setAttributesPercentage(numAttributesPercentageOption.getValue());
		baseLearner.resetLearning();
		for (int i = 0; i < this.ensemble.length; i++) {
			this.ensemble[i] = (AMRulesMultiLabelLearner) baseLearner.copy();
			this.ensemble[i].setRandomSeed(this.classifierRandom.nextInt());
			this.errorMeasurer[i]=(MultiLabelErrorMeasurer)measurer.copy();
		}
		this.isRegression = (baseLearner instanceof MultiTargetRegressor);
	}

	public void trainOnInstanceImpl(MultiLabelInstance instance) {
		for (int i = 0; i < this.ensemble.length; i++) {
			Instance inst=instance.copy();
			int k = 1;
			if ( this.useBaggingOption.isSet()) {
				k = MiscUtils.poisson(1.0, this.classifierRandom);
			} 
			if (k > 0) {
				//Instance weightedInst = transformInstance(inst,i);
				inst.setWeight(inst.weight() * k);
				//estimate error
				Prediction p=ensemble[i].getPredictionForInstance(inst);
				if(p!=null)
					errorMeasurer[i].addPrediction(p, instance);	
				//train learner
				this.ensemble[i].trainOnInstance(inst);
			}
		}
	}

	@Override
	public Prediction getPredictionForInstance(MultiLabelInstance inst) {
		Prediction vote=null;
		//ErrorWeightedVote combinedVote = (ErrorWeightedVote)((ErrorWeightedVote) votingTypeOption.getPreMaterializedObject()).copy();
		ErrorWeightedVoteMultiLabel combinedVote = (ErrorWeightedVoteMultiLabel)((ErrorWeightedVoteMultiLabel) getPreparedClassOption(this.votingFunctionOption)).copy();
		StringBuilder sb = null;
		if (VerbosityOption.getValue()>1)
			sb=new StringBuilder();

		for (int i = 0; i < this.ensemble.length; i++) {
			// transformInstance method visibility changed from private to protected in RandomRules
			ErrorWeightedVoteMultiLabel v = ((AMRulesMultiLabelLearner) this.ensemble[i]).getVotes(inst);
			if (VerbosityOption.getValue()>1)
				sb.append(v.getPrediction() + ", " + " E: " + v.getWeightedError() + " ");
			/*if (!this.isRegression){
					v.normalize();
			}*/
			Prediction p=v.getPrediction();
			if(p!=null){
				if(this.votingTypeOption.getChosenIndex()==0){//Overall error estimation
					combinedVote.addVote(p,errorMeasurer[i].getCurrentErrors());
				}
				else //Error estimation over the rules that cover the example
					combinedVote.addVote(p, v.getOutputAttributesErrors());
			}
		}
		vote=combinedVote.computeWeightedVote();
		if (VerbosityOption.getValue()>1){
			sb.append(vote  + ", ").append(inst.classValue()); 
			System.out.println(sb.toString());
		}
		return vote; 
	}


	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		Measurement [] baseLearnerMeasurements=((AMRulesMultiLabelLearner) getPreparedClassOption(this.baseLearnerOption)).getModelMeasurements();
		int nMeasurements=baseLearnerMeasurements.length;
		Measurement [] m=new Measurement[nMeasurements+1];

		for(int i=0; i<baseLearnerMeasurements.length; i++)
			m[i+1]=baseLearnerMeasurements[i];
		
		int ensembleSize=0;
		if(this.ensemble !=null){	
			ensembleSize=this.ensemble.length;
			for(int i=0; i<nMeasurements; i++){
				double value=0;
				for (int j=0; j<ensembleSize; ++j){
					value+=ensemble[j].getModelMeasurements()[i].getValue();
				}
				m[i+1]= new Measurement("Avg " + baseLearnerMeasurements[i].getName(), value/ensembleSize);
			}
		}
	
		m[0]=new Measurement("ensemble size", ensembleSize);
		
		return m;
	}

	@Override
	public void getModelDescription(StringBuilder out, int indent) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRandomizable() {
		return true;
	}



	}
