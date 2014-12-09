package net.sf.rails.ui.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.*;

import net.sf.rails.common.Config;
import net.sf.rails.common.ResourceLoader;
import net.sf.rails.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;


/**
 * This class handles loading our tile images. It provides BufferedImages to be
 * associated with the Hex.
 */
public class ImageLoader {

    private static final Logger log =
            LoggerFactory.getLogger(ImageLoader.class);
    
    private static final int SVG_WIDTH = 392;
    private static final int SVG_HEIGHT = 340;

    private final Map<String, Document> svgMap = Maps.newHashMap();
    private final Map<String, URI> tile2SVG = Maps.newHashMap();
    
    private final HashBasedTable<String, Integer, BufferedImage> tileImages = 
            HashBasedTable.create();

    private float[] zoomFactors = new float[21];

    //defines adjustment of zoom factor (should be close to 1) 
    //(used for perfect-fit sizing that requires arbitrary zoom)
    private double zoomAdjustmentFactor = 1;

//    private float svgWidth = 75;
//    private float svgHeight = (float) (svgWidth * 0.5 * Math.sqrt(3.0));
    
    private String svgTileDir = "tiles/svg";
    private String tileRootDir = Config.get("tile.root_directory");
    private String directory;


    public ImageLoader() {
        if (Util.hasValue(tileRootDir) && !tileRootDir.endsWith("/")) {
            tileRootDir += "/";
        }
        directory = (tileRootDir + svgTileDir);
    }

    private BufferedImage getSalamanderTile(String tileID, float zoomFactor) {

        SVGUniverse svgUniverse = SVGCache.getSVGUniverse();

        String fn = "tile" + tileID + ".svg";
        try {
            if (!svgMap.containsKey(tileID)) {
                // Step 3: load file into SVG universe (loadSVG only loads if not contained already)
                URL docURL = ResourceLoader.getURL(fn, directory);
                URI docURI = svgUniverse.loadSVG(docURL);
                // Cache the uri
                tile2SVG.put(tileID, docURI);
                log.debug("SVG loading for tile id " + tileID + " succeeded ");
            }
        } catch (Exception e) {
            log.error("SVG loading for tile id " + tileID + " failed with "
                      + e);
            return null;
        }
  
        // define buffered image of required size
        BufferedImage image = new BufferedImage(SVG_WIDTH, SVG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHints(GUIGlobals.getRenderingHints());
        SVGDiagram diagram = svgUniverse.getDiagram(tile2SVG.get(tileID));
        
        // rendering of the SVG diagram
        try {
            diagram.render(g2d);
        } catch (SVGException e) {
            log.error("SVG rendering for tile id " + tileID + " failed with "
                    + e);
            g2d.dispose();
            return null;
        }
        return image;
    }
    
 
    public BufferedImage getTile(String tileID, int zoomStep) {
        if (tileImages.contains(tileID, zoomStep)) {
            return tileImages.get(tileID, zoomStep);
        } else {
            BufferedImage image = getSalamanderTile(tileID, getZoomFactor(zoomStep));
            tileImages.put(tileID, zoomStep, image);
            return image;
        }
    }

    public float getZoomFactor (int zoomStep) {
        if (zoomStep < 0) zoomStep = 0;
        else if (zoomStep > 20) zoomStep = 20;
        if (zoomFactors[zoomStep] == 0.0) {
            zoomFactors[zoomStep] = (float) (zoomAdjustmentFactor * Math.pow(2.0, 0.25*(zoomStep-10)));
        }
        return (float) (zoomFactors[zoomStep]* GUIGlobals.getMapScale());

    }
    
    /**
     * @param zoomAdjustmentFactor Additional factor applied to zoom factor. Used
     * for precisely adjusting zoom-step based zoom factors for perfect fit requirements.  
     */
    public void setZoomAdjustmentFactor (double zoomAdjustmentFactor) {
        this.zoomAdjustmentFactor = zoomAdjustmentFactor;
        
        //invalidate buffered zoom step zoom factors
        for (int i = 0 ; i < zoomFactors.length ; i++) {
            zoomFactors[i] = 0;
        }
        
        //invalidate buffered tile scalings
        tileImages.clear();
    }
    
    public void resetAdjustmentFactor() {
        setZoomAdjustmentFactor(1);
    }


}
