package varpedia.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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

public class FlickrTask extends Task<Integer> {

	private static List<String> search(String term, int count) throws FlickrException {
		if (count == 0) {
			return new ArrayList<String>();
		}
		
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
		
		List<String> list = new ArrayList<String>();
		Flickr f = new Flickr(key, secret, new REST());
		PhotosInterface photos = f.getPhotosInterface();
		SearchParameters params = new SearchParameters();
		params.setText(term);
		params.setSort(SearchParameters.RELEVANCE);
		params.setMedia("photos");
		PhotoList<Photo> photoList = photos.search(params, count, 0);
			
		for (Photo p : photoList) {
			list.add("https://farm" + p.getFarm() + ".staticflickr.com/" + p.getServer() + "/" + p.getId() + "_" + p.getSecret() + ".jpg");
		}
		
		return list;
	}
	
	private String _term;
	private int _images;
	
	public FlickrTask(String term, int images) {
		_term = term;
		_images = images;
	}
	
	@Override
	protected Integer call() throws Exception {

		List<String> list = search(_term, _images);
		int id = 0;
		
		for (String url : list) {
			URL source = new URL(url);
			File dest = new File("appfiles/image" + Integer.toString(id) + ".jpg");

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
