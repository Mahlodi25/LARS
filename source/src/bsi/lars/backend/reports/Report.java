package bsi.lars.backend.reports;

import java.io.InputStream;
import java.util.Date;
import java.util.Vector;

import bsi.lars.backend.Backend;
import bsi.lars.backend.data.Case;
import bsi.lars.backend.data.User;
import bsi.lars.backend.datastore.layers.Category;
import bsi.lars.backend.datastore.layers.Measure;

public abstract class Report {

	public InputStream getXSL() {
		return PriorizedMeasures.class.getResourceAsStream("/resources/" + getClass().getSimpleName() + ".xsl");
	}
	

	protected String generateReport(String name, User user, Case _case, int score, String content) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		sb.append("<report>");
		sb.append("<name>");
		sb.append(toCData(name));
		sb.append("</name>\n");
		sb.append(user.toXML());
		sb.append(_case.toXML());
		sb.append("<score>");
		sb.append(score);
		sb.append("</score>");
		sb.append(content);
		sb.append("</report>");
		
		return sb.toString();
	}

	protected String toCData(String data) {
		return "<![CDATA["+data+"]]>";
	}

	protected abstract Vector<Category> getFilteredCategories(Backend backend);
	protected abstract Vector<Measure> getFilteredMeasures(Backend backend);
	

	/**
	 * Hier besteht die M�glichkeit, die Ausgabe der Kategorien zu beeinflussen.
	 * @param categories
	 * @return Gibt die Ausgabe f�r die �bergebenen Kategorien zur�ck
	 * @throws Exception
	 */
	protected abstract String generateCategoriesOutput(Vector<Category> categories) throws Exception;
	
	
	/**
	 * Hier besteht die M�glichkeit, die Ausgabe der Ma�nahmen zu beeinflussen.
	 * @param measures
	 * @return Gibt die Ausgabe f�r die �bergebenen Ma�nahmen zur�ck
	 * @throws Exception
	 */
	protected abstract String generateMeasuresOutput(Vector<Measure> measures) throws Exception;
	
	/**
	 * Hier besteht die M�glichkeit, zus�tzliche Daten f�r den Report zur�ckzugeben.
	 * @return null, wenn keine weiteren Informationen ben�tigt werden. Wenn daten zur�ck gegeben werden, werden diese in den <info></info> Block des generierten XML geschrieben.
	 */
	protected abstract String generateAdditionalDetails();
	
	@SuppressWarnings("unchecked")
	public String getXML(Backend backend) throws Exception {
		Vector<Category> categories = getFilteredCategories(backend);
		Vector<Measure> measures = getFilteredMeasures(backend);
		
		StringBuilder content = new StringBuilder();
		
		content.append("<date>\n");
		{
			content.append(
							toCData(
									java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG, java.util.Locale.getDefault()).format(new Date())
									)
							);
		}
		content.append("</date>\n");
		
		String additionalReportDetails = generateAdditionalDetails();
		if(additionalReportDetails != null) {
			content.append(additionalReportDetails);
			content.append("\n");
		}


		content.append("<categories>\n");
		content.append(generateCategoriesOutput((Vector<Category>) categories.clone()));
		content.append("</categories>\n");
		
		
		content.append("<measures>\n");
		content.append(generateMeasuresOutput((Vector<Measure>) measures.clone()));
		content.append("</measures>\n");
		
		int[] answercount = new int[Backend.getInstance().getStati().length];
		int unansweredcount = 0;
//		int[] scorecount = new int[4];
		//                                                                                 Scores
		int[][] measurescorecountbyanswer = new int[Backend.getInstance().getStati().length][4];
		//                                                   M�gliche Antworten
		int[] measurescoresbyunanswered = new int[4];
		
		for(Measure m : measures) {
			int answer = m.getAnswer().getComplexAnswer();
			if(answer == 0) {
				System.out.println();
			}
			if(answer > 0) {
				++answercount[answer - 1];
				++measurescorecountbyanswer[answer - 1][m.getMeasureScore()];
			}else{
				++unansweredcount;
				++measurescoresbyunanswered[m.getMeasureScore()];
			}
		}
		
		int answerindex = answercount.length - 1;
		//Nicht vorhandene Kuchenst�cke ausblenden.
		while(answerindex > 0 && answercount[answerindex] > 0) {
			--answerindex;
		}
		if(answerindex >= 0) {
			content.append("<answerstatistic>\n");
			for(int i = 0 ; i <= answerindex ; ++i) {
				content.append("<pieslice name=\"");
				content.append(Backend.getInstance().getStati()[i]);
				content.append("\">");
				content.append(answercount[i]);
				content.append("</pieslice>\n");
			}
			if(unansweredcount > 0) {
				content.append("<pieslice name=\"");
				content.append("nicht beantwortet");
				content.append("\">");
				content.append(unansweredcount);
				content.append("</pieslice>\n");
			}
			content.append("</answerstatistic>\n");
		}

		String[] scorenames = new String[]{"KO", "Kritikalit�t 1", "Kritikalit�t 2", "Kritikalit�t 3"};
		
		for(int answer = 0; answer < measurescorecountbyanswer.length; answer++) {
			int[] scorecount = measurescorecountbyanswer[answer];

			int sum = 0;
			for(int i : scorecount) {
				sum += i;
			}
			if(sum == 0) {
				continue;
			}
			
			content.append("<scorestatistic>\n");
			content.append("<answer>" + Backend.getInstance().getStati()[answer] + "</answer>\n");
			for(int i = 0 ; i < scorecount.length ; ++i) {
				content.append("<pieslice name=\"");
				content.append(scorenames[i]);
				content.append("\">");
				content.append(scorecount[i]);
				content.append("</pieslice>\n");
			}
			content.append("</scorestatistic>\n");
		}
		if(unansweredcount > 0) {
			content.append("<scorestatistic>\n");
			content.append("<answer>" + "nicht beantwortet" + "</answer>\n");
			for(int i = 0 ; i < measurescoresbyunanswered.length ; ++i) {
				content.append("<pieslice name=\"");
				content.append(scorenames[i]);
				content.append("\">");
				content.append(measurescoresbyunanswered[i]);
				content.append("</pieslice>\n");
			}
			content.append("</scorestatistic>\n");
		}
		
		return generateReport(getName(), backend.getCurrentUser(), backend.getCurrentCase(), backend.getScore(), content.toString());
	}

	protected abstract String getName();
	public abstract String getDefaultFileName();
}
