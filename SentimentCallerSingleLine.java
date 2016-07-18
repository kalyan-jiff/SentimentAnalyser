



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager.*;
import java.util.logging.Logger;

public class SentimentCallerSingleLine {

	private static int count;

	public static void main(String[] args) {
		
		String line = null;
		int numPositivePosts=0;
		int numNeutralPosts=0;
		int numNegativePosts =0;
		int numQuestions = 0;
		int numSkipped = 0;
		
		
		//Format :  <Post>, <Overall Sentiment>, <Overall Sentiment Reason>, <Overall Sentiment Reason Specifics>
		//BufferedReader br = null;
		//FileWriter fileWriter = null;
		final String COMMA_DELIMITER = ",";
		final String HEADER = "Post, Is Question,Overall Sentiment,Overall Sentiment Reason,Overall Sentiment Reason Specifics";
		final String NEW_LINE_SEPARATOR = "\n";
		
		
		List <String> negkeywords = new ArrayList<String>();
		negkeywords.add("not syncing");
		negkeywords.add("issue");
		negkeywords.add("not able");
		negkeywords.add("sync issue");
		negkeywords.add("doesn't sync");
		negkeywords.add("trouble syncing");
		negkeywords.add("can't seem to");
	
		List <String> poskeywords = new ArrayList<String>();
		poskeywords.add("love");
		poskeywords.add("loving");
		poskeywords.add("thrilled");
		poskeywords.add("great job");
		poskeywords.add("well done");
		poskeywords.add("terrific");
		poskeywords.add("thank you");
			
			
				//THis parses out the sententences in a paragraph and give a score for each sentence
				int score = 2; // Default as Neutral. 0 = very negative, 1 = negative, 2 = neutral, 3 = positive, and 4 = very positive.
				
				
				//Printing Rules -->
				//Any post with minLineScore = 0;
				//Any post with more than one negative (more than one 1)
				//Any post with more than one positive (more than one 3)
				//Any post with more maxLineScore =4
				
				
				line = "I can already tell that this will motivate me. I have my Fitbit flex for one day and surprise, I like seeing data. My evening walk was longer since I knew this thing was tracking.";
				
						
						int minLineScore=2;
						int maxLineScore=2;
						int numNeg = 0;
						int numPos = 0;
						String minLine = "";
						String maxLine="";
						Boolean isnegkeywordMatch = false;
						Boolean isposkeywordMatch = false;
						List <String> negSentimentLines= new ArrayList<String>();
						List <String> posSentimentLines= new ArrayList<String>();
						String negKeyWordMatched = "";
						String posKeyWordMatched = "";
						String overallPostSentiment = "";
						String overallPostSentimentReason = "";
						String overallPostSentimentReasonSpecifics = "";
						String overallPostSentimentReasonCombo = "";
						Boolean isQuestion = false;
						
						RedwoodConfiguration.empty().capture(System.err).apply();
	
					
						//Set Properties
				        Properties props = new Properties();
				        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
				        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
				        
				        //Process the line
				        
				       
		        				
					        
					        Annotation annotation = pipeline.process(line);
					        
					        
					        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
					        for (CoreMap sentence : sentences) 
					        {
					        	
					        	  String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
						          Tree tree = sentence.get(SentimentAnnotatedTree.class);
						          score = RNNCoreAnnotations.getPredictedClass(tree);
						          
						          System.out.printf("Sentence: %s\n,Sentiment: %s\n, Score %s\n", sentence.toString(), sentiment, score);
						          
						          if (score<=minLineScore)
						          {
						        	  minLineScore = score;
						        	  minLine = sentence.toString();
						          }
						        	 
						          
						          if (score>=maxLineScore)
						          {
						        	  maxLineScore = score;
						        	  maxLine = sentence.toString();
						          }
						          
						          if (score == 1)
						          {
						        	  numNeg = numNeg + 1;
						        	  negSentimentLines.add(sentence.toString());
						          }
						          
						          if (score == 3)
						          {
						        	  numPos = numPos + 1;
						        	  posSentimentLines.add(sentence.toString());
						          }
						          
						          for (int iter=0; iter<negkeywords.size(); iter++)
						          {
						        	  String toMatch = "(?i)(.*)"+negkeywords.get(iter)+"(.*)";
						        	  if (sentence.toString().matches(toMatch) == true)
						        	  {
						        		  isnegkeywordMatch = true;
						        		  negKeyWordMatched = negkeywords.get(iter);
						        	  }
						        		  
						          }
						          
						          for (int iter=0; iter<poskeywords.size(); iter++)
						          {
						        	  String toMatch = "(?i)(.*)"+poskeywords.get(iter)+"(.*)";
						        	  if (sentence.toString().matches(toMatch) == true)
						        	  {
						        		  isposkeywordMatch = true;
						        		  posKeyWordMatched = poskeywords.get(iter);
						        	  }
						        		  
						          }
	      
					        }
						          
					        RedwoodConfiguration.current().clear().apply();      
					        
					        
					        // Deal with negative sentiment or keyword match first
					        if (minLineScore == 0 || numNeg >=2 || isnegkeywordMatch )
					        {
					        	overallPostSentiment = "Negative";
					        	
					        	
						        if (minLineScore ==0 )
						        {
						        	overallPostSentimentReason = "Very Negative Statement";
						        	overallPostSentimentReasonSpecifics = minLine;
						        	overallPostSentimentReasonCombo = "Very Negative Statement:" + minLine;
						        	//System.out.println(overallPostSentimentReasonCombo+"\n");
					        	
						        }
						        
						        if (numNeg >=2)
						        {
						        	overallPostSentimentReason = "Multiple Negative Statements";
						        	overallPostSentimentReasonSpecifics = "N/A";
						        	overallPostSentimentReasonCombo = "Multiple Negative Statements:";
						        	System.out.println("NEGATIVE POST ALERT: MULITPLE NEGATIVE SENTIMENTS\n");
						        	for (int iter = 0; iter < negSentimentLines.size(); iter++)
						        		System.out.printf(negSentimentLines.get(iter)+"\n");
						        }
						        
						        if (isnegkeywordMatch)
						        {
						        	overallPostSentimentReason = "Negative Keyword Match";
						        	overallPostSentimentReasonSpecifics = negKeyWordMatched;
						        	overallPostSentimentReasonCombo = "Negative Keyword Match:" + negKeyWordMatched;
						        	//System.out.println(overallPostSentimentReasonCombo+"\n");
						        }
						        	
					        }
					        
					        //If not negative, deal with positive
					        
					        else if(maxLineScore ==4 || numPos >=2  || isposkeywordMatch)
					        {
					        	overallPostSentiment = "Positive";
					        	if (maxLineScore ==4)
						        {
					        		overallPostSentimentReason = "Very Positive Statement";
					        		overallPostSentimentReasonSpecifics = maxLine;
					        		overallPostSentimentReasonCombo = "Very Positive Statement:" + maxLine;
					        		//System.out.println(overallPostSentimentReasonCombo+"\n");
						        }
						        
						        if (numPos >=2)
						        {
						        	overallPostSentimentReason = "Multiple Positive Statements";
						        	overallPostSentimentReasonSpecifics = "N/A";
						        	overallPostSentimentReasonCombo = "Multiple Positive Statements:";
						        	//System.out.println("POSTIVE POST ALERT: MULITPLE POSITIVE SENTIMENTS\n");
						        	//for (int iter = 0; iter < posSentimentLines.size(); iter++)
						        		//System.out.printf(posSentimentLines.get(iter)+"\n");
						        }
						        if (isposkeywordMatch)
						        {
						        	overallPostSentimentReason = "Positive Keyword Match";
						        	overallPostSentimentReasonSpecifics = posKeyWordMatched;
						        	overallPostSentimentReasonCombo = "Positive Keyword Match:" + posKeyWordMatched;
						        	//System.out.println(overallPostSentimentReasonCombo+"\n");
						        }
						        	
					        }
					        
					        else {
					        	overallPostSentiment = "Neutral";
					        	overallPostSentimentReason = "N/A";
					        	overallPostSentimentReasonSpecifics = "N/A";
					        }
					        	
					        if (overallPostSentiment == "Positive")
					        	numPositivePosts = numPositivePosts +1;
					        else if (overallPostSentiment == "Negative")
					        	numNegativePosts = numNegativePosts +1;
					        if (overallPostSentiment == "Neutral")
					        	numNeutralPosts = numNeutralPosts +1;
					        
					        System.out.println("\n----------------\n");
					        System.out.println(count + ":" + line+"\n"); 
					        System.out.printf("Post Has Question?:%b\n", isQuestion); 
					        System.out.printf("Overall Sentiment: %s\nOverall Sentiment Reason: %s\nSpecifics: %s\n", overallPostSentiment,overallPostSentimentReason, overallPostSentimentReasonSpecifics); 
		
	        				
				        
				        
			        	
						//line = br.readLine();
						count++;

				
				System.out.println("-------------- Summary ------------\n");
				System.out.printf("Number of Posts: %d\nNumber of Questions: %d\nNumber of Skipped Posts: %d\nNumber of Positive Posts: %d\nNumber of Negative Posts: %d\nNumber of Nuetral Posts: %d\n", count-1,numQuestions, numSkipped, numPositivePosts,numNegativePosts, numNeutralPosts); 
			     
				
			    
			       	        
			}
			
	}



		
	
