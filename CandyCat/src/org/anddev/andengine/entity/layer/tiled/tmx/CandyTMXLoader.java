package org.anddev.andengine.entity.layer.tiled.tmx;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.opengl.texture.TextureManager;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;

public class CandyTMXLoader extends TMXLoader {
	private final String beginning;
	private static final String end = "</data></layer></map>";

	private final Context mContext;
	private final TextureManager mTextureManager;
	private final TextureOptions mTextureOptions;
	private final ITMXTilePropertiesListener mTMXTilePropertyListener;

	public CandyTMXLoader(final String theme,final Context pContext,final TextureManager pTextureManager,final TextureOptions pTextureOptions,final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		super(pContext, pTextureManager, pTextureOptions, pTMXTilePropertyListener);
		beginning = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><map version=\"1.0\" orientation=\"orthogonal\" width=\"24\" height=\"18\" tilewidth=\"64\" tileheight=\"64\"><tileset source=\"levels/themes/"+theme+".cct\"/><layer width=\"24\" height=\"18\"><data encoding=\"base64\" compression=\"gzip\">";
		mContext = pContext;
		mTextureManager = pTextureManager;
		mTextureOptions = pTextureOptions;
		mTMXTilePropertyListener = pTMXTilePropertyListener;
	}
	
	@Override
	public TMXTiledMap load(final InputStream pInputStream) throws TMXLoadException {
		try{
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();

			final XMLReader xr = sp.getXMLReader();
			final TMXParser tmxParser = new TMXParser(this.mContext, this.mTextureManager, this.mTextureOptions, this.mTMXTilePropertyListener);
			xr.setContentHandler(tmxParser);
			
			final List<InputStream> streams = Arrays.asList(new ByteArrayInputStream(beginning.getBytes()),pInputStream,new ByteArrayInputStream(end.getBytes()));
			
			xr.parse(new InputSource(new BufferedInputStream(new SequenceInputStream(Collections.enumeration(streams)))));

			return tmxParser.getTMXTiledMap();
		} catch (final SAXException e) {
			throw new TMXLoadException(e);
		} catch (final ParserConfigurationException pe) {
			/* Doesn't happen. */
			return null;
		} catch (final IOException e) {
			throw new TMXLoadException(e);
		}
	}
}
