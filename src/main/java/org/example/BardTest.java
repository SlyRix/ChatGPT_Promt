package org.example;

import com.pkslow.ai.AIClient;
import com.pkslow.ai.GoogleBardClient;
import com.pkslow.ai.domain.Answer;

public class BardTest
{
    public static void main(String[] args)
    {
        String token = "ZgiXRZupInt7HlViV75XsJXqzVnMLv_7NXSjS1sDvn6cfHfHFWXkDAGVOPYLenMWdqK6pA.;sidts-CjEBSAxbGc66uYvxlukjQM52WOzD1UlZ1vAnqbJr65lvDkPJF3r9NS9YTxbYSbDxoaGUEAA";
        AIClient client = new GoogleBardClient(token);
        Answer answer = client.ask("what is 3+2");
        System.out.println(answer.getChosenAnswer());
    }
}
