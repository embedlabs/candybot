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
import com.embedstudios.candycat.R;

public class CandyTMXLoader extends TMXLoader {
	private final String beginning;
	private static final String end = "</data></layer></map>";

	private final Context mContext;
	private final TextureManager mTextureManager;
	private final TextureOptions mTextureOptions;
	private final ITMXTilePropertiesListener mTMXTilePropertyListener;

	public CandyTMXLoader(final String theme,final Context pContext,final TextureManager pTextureManager,final TextureOptions pTextureOptions,final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		super(pContext, pTextureManager, pTextureOptions, pTMXTilePropertyListener);
		beginning = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><map version=\"1.0\" orientation=\"orthogonal\" width=\"24\" height=\"18\" tilewidth=\"64\" tileheight=\"64\"><tileset name=\"Background\" tilewidth=\"64\" tileheight=\"64\" spacing=\"3\" margin=\"2\"><image source=\"gfx/"+theme+"/bg_tileset.png\" width=\"269\" height=\"202\" /><tile id=\"0\"><properties><property name=\"wall\" value=\"true\" /></properties></tile><tile id=\"1\"><properties><property name=\"pipe\" value=\"true\" /></properties></tile><tile id=\"2\"><properties><property name=\"pipe\" value=\"true\" /></properties></tile><tile id=\"3\"><properties><property name=\"laser\" value=\"true\" /></properties></tile><tile id=\"4\"><properties><property name=\"laser\" value=\"true\" /></properties></tile><tile id=\"5\"><properties><property name=\"laser\" value=\"true\" /></properties></tile><tile id=\"6\"><properties><property name=\"teleporter1\" value=\"true\" /></properties></tile><tile id=\"7\"><properties><property name=\"teleporter2\" value=\"true\" /></properties></tile><tile id=\"8\"><properties><property name=\"wall\" value=\"true\" /><property name=\"ice\" value=\"true\" /></properties></tile><tile id=\"9\"><properties><property name=\"pipe\" value=\"true\" /><property name=\"ice\" value=\"true\" /></properties></tile><tile id=\"10\"><properties><property name=\"pipe\" value=\"true\" /><property name=\"ice\" value=\"true\" /></properties></tile><tile id=\"11\"><properties><property name=\"wall\" value=\"true\" /><property name=\"lava\" value=\"true\" /></properties></tile></tileset><layer width=\"24\" height=\"18\"><data encoding=\"base64\" compression=\"gzip\">";
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
