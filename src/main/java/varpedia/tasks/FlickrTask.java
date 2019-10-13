package varpedia.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;

import javafx.concurrent.Task;

/**
 * Background task that handles all interactions with the Flickr API (via the Flickr4Java library), specifically
 * the downloading of a user-specified quantity of images.
 * Requires a Flickr API key to work, which is stored in src/main/resources/varpedia/key.txt.
 *
 * @author Tudor Zagreanu
 */
public class FlickrTask extends Task<Integer> {

	private static List<String> search(String term, int count) throws FlickrException {
		//Well, they wanted nothing
		if (count == 0) {
			return new ArrayList<String>();
		}
		
		//Read the api key in
		String key = null;
		String secret = null;
		
		try (BufferedReader keys = new BufferedReader(new InputStreamReader(FlickrTask.class.getResourceAsStream("/varpedia/key.txt"))))
		{
			key = keys.readLine();
			secret = keys.readLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		//Perform the actual search
		List<String> list = new ArrayList<String>();
		Flickr f = new Flickr(key, secret, new REST());
		PhotosInterface photos = f.getPhotosInterface();
		SearchParameters params = new SearchParameters();
		params.setText(term);
		params.setSort(SearchParameters.RELEVANCE);
		params.setMedia("photos");
		PhotoList<Photo> photoList = photos.search(params, count, 0);
		
		//The official sanctioned by flickr method for turning photos into URLs
		for (Photo p : photoList) {
			list.add("https://farm" + p.getFarm() + ".staticflickr.com/" + p.getServer() + "/" + p.getId() + "_" + p.getSecret() + ".jpg");
		}
		
		return list;
	}
	
	private int _images;
	
	/**
	 * @param images The number of images
	 */
	public FlickrTask(int images) {
		_images = images;
	}
	
	@Override
	protected Integer call() throws Exception {
		//Read the search term in
        String term = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("appfiles/search-term.txt"), StandardCharsets.UTF_8));
            term = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (term == null) {
        	throw new IllegalStateException("No search term");
        }
        
        //Run the search
        List<String> list = search(term, _images);
		int id = 0;
		
		for (String url : list) {
			URL source = new URL(url);
			File dest = new File("appfiles/image" + Integer.toString(id) + ".jpg");

			//Download the image, saving as appfiles/image<id>.jpg
			try (InputStream input = source.openStream(); FileOutputStream output = new FileOutputStream(dest)) {
				byte[] transfer = new byte[4096];
				int count;
				
				while ((count = input.read(transfer)) != -1) {
					if (isCancelled()) {
						return null;
					}
					
					output.write(transfer, 0, count);
				}
			}
			
			id++;
		}

		return id;
	}

}
