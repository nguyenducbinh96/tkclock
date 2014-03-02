package com.tkclock.voice.user_interaction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;


public class Predictor {
  //for n-grams of names
  private static HashMap<String, Integer> shingles = new HashMap<String, Integer>();

  private static int keys = 1;
 
  private String[][] voice = {
      {"POS", "pulse", "paused", "pas", "Pub", "how old", "how", "Howell", "howl", "how lol", "house", "how's", "hows", 
      "House", "Houzz", "pas", "Paz", "paz", "poz"},
      {"top", "stop", "Top", "pop", "top:", "Tom", "talk", "stock", "stopped", "dog", "taupe", "poke", "Pope", "hope", "dope"},
      {"business", "yes miss", "miss", "you sneeze", "is this", "Disney", "Disney is", "Disney's", "did sneeze", 
        "miss me", "dismissed", "business", "this myth", "miss me", "Disney is", "needs me", "Disney s", 
        "Disney this", "Disney hits"},
      {"done up", "Dunlop", "that up", "the knot", "turned off", "ton of", "TURN OFF", "TeleNav", "turn Off", "turned off"},
      {"of", "all off", "all of", "auf", "of", "0", "o", "also", "OOV", "Off"},
      {"repeated", "3 peat", "Rapide", "I repeat", "repeated", "repeat", "free kid", "rated", "wicked", 
        "I repeat", "repeat it", "speed", "we did", "the beat", "faded"},
      {"bag", "beg", "dag", "bag it", "back", "bag", "Mac", "backed", "back to", "night", "back", "Mac", 
          "nite", "bike", "bag", "like", "beg", "mag"},
      {"next:", "Next", "text", "next time", "naked", "neck", "nake", "snake", "next day"},
      {"Facebook", "facebook:", "face book", "Facebook app"},
      {"Gmail", "shemale", "she male", "Shemale", "She Male", "g mail", "Gmail app", "G mail", "good mail", "get married", 
        "good man", "good male", "give me lol"},
      {"you mail", "email", "YouMail", "email:", "female", "Mel", "mal", "male", "email:", "e-mail:", "e-mail"},
      {"weather", "what's a", "what's up", "what is a", "whats up", "what's up", "glad that", "what's that", "was that"},
      {"News", "news:", "muse", "noose", "new", "noo", "nu", "new:", "news", "noo", "nu", "Neil", "used"}
    };
  
  private String commands[] = {"pause", "stop", "dismiss", "turn off", "off", 
    "repeat", "back", "next",
    "facebook", "gmail", "email", "weather", "news"};

  private Map<String, String> sound2command = new HashMap<String, String>();
  
  public Predictor() {
    for(int i = 0; i < commands.length; i ++) {
      for(int j = 0; j < voice[i].length; j++) {
        if(sound2command.containsKey(voice[i][j])) {
          // confused here. Just remove
          sound2command.remove(voice[i][j]);
        }
        else {
          sound2command.put(voice[i][j], commands[i]);
        }
      }
    }
  }
  
  public String predictFromSimilarSound(String sound) {
    return sound2command.get(sound);
  }
  
  public String predictFromSimilarString(String command) {
    String nearestString = null;
    double sim = 0.3; // threshold
    Counter counter = getCounter(command);
    for(int i = 0; i < commands.length; i ++) {
      double d = getExactJaccard(counter, getCounter(commands[i]));
      if(sim < d) {
        // update
        sim = d;
        nearestString = commands[i];
      }
    }
    return nearestString;
  }
  
  @SuppressWarnings("unused")
  private double getExactJaccard(Counter index1, Counter index2) {
    double min, max, s_min = 0, s_max = 0, bound = 0;
    int c1, c2, s_c1 = 0, s_c2 = 0;

    // merge join
    int pos = 0;
    int[] keys1 = index1.keySet();
    int[] keys2 = index2.keySet();
    for (int i = 0; i < keys1.length; i++) {
      int key = keys1[i];
      while (pos < keys2.length && keys2[pos] < key)
        pos++;

      if (pos == keys2.length) {
        // nothing to do more
        break;
      }
      if (keys2[pos] == key) {
        // matching
        c1 = index1.getCountFromIndex(i);
        c2 = index2.getCountFromIndex(pos);
        min = Math.min(c1, c2);
        max = Math.max(c1, c2);
        s_min += min;
        s_max += max;
        s_c1 += c1;
        s_c2 += c2;

        // Early threshold break for pairwise counter comparison
        bound += max - min;

      }

      else {
        // nothing to do
      }
    }

    return s_min / (index1.totalCount + index2.totalCount - s_min);
  }
  
  
  private Counter getCounter(String s) {
    // s = s.replaceAll("['-()*&^%$#@!;]","");
    String str = s.replaceAll("_", " ");
    OpenIntIntHashMap entries = new OpenIntIntHashMap();
    // Counter counter = new Counter(s);
    int totalCount = 0;
    StringTokenizer tokenizer = new StringTokenizer(str);
    while (tokenizer.hasMoreTokens()) {
      String tok = tokenizer.nextToken();
      if (tok.length() < 2) {
        // if tok has less than 3 characters
        // then take tok as a shingle
        String shingle = tok;
        Integer key = shingles.get(shingle);
        if (key == null) {
          // System.out.println(shingle + " : " + keys);
          shingles.put(shingle, keys);
          // counter.incrementCount(keys);
          entries.put(keys, entries.get(keys) + 1);
          totalCount++;
          keys++;
        } else {
          // counter.incrementCount(key.intValue());
          entries.put(key.intValue(), entries.get(key.intValue()) + 1);
          totalCount++;
        }
        continue;
      }
      for (int i = 0; i < tok.length() - 1; i++) {
        // String shingle = tok; //"";
        String shingle = "";
        for (int j = i; j < Math.min(tok.length(), i + 2); j++)
          shingle += tok.charAt(j);
        shingle = shingle.toLowerCase();
        Integer key = shingles.get(shingle);
        if (key == null) {
          // System.out.println(shingle + " : " + keys);
          shingles.put(shingle, keys);
          // counter.incrementCount(keys);
          entries.put(keys, entries.get(keys) + 1);
          totalCount++;
          keys++;
        } else {
          // counter.incrementCount(key.intValue());
          entries.put(key.intValue(), entries.get(key.intValue()) + 1);
          totalCount++;
        }
      }
    }
    // Counter counter = new Counter(s);
    // insert into counter from hashmap here...

    // return new Counter(s, entries, totalCount);

    int[] keys = new int[entries.size()];
    IntArrayList l = entries.keys();
    for (int i = 0; i < entries.size(); i++)
      keys[i] = l.get(i);

    // sort keys arrays
    Arrays.sort(keys);
    int[] vals = new int[keys.length];
    for (int i = 0; i < keys.length; i++)
      vals[i] = entries.get(keys[i]);

    entries = null;
    return new Counter(s, keys, vals, totalCount);
  }
  
  

}
