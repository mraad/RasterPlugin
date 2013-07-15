package com.esri;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 */
public class RasterPlugin extends JavaPlugin implements Listener
{
    public final Map<String, RasterInfo> rasterInfoMap = new HashMap<String, RasterInfo>();

    private interface DataReader
    {
        public float readData(final DataInputStream dataInputStream) throws IOException;
    }

    private final class LSBDataReader implements DataReader
    {
        @Override
        public float readData(final DataInputStream dataInputStream) throws IOException
        {
            final int b0 = dataInputStream.readUnsignedByte();
            final int b1 = dataInputStream.readUnsignedByte();
            final int b2 = dataInputStream.readUnsignedByte();
            final int b3 = dataInputStream.readUnsignedByte();
            final int nume = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
            return Float.intBitsToFloat(nume);
        }
    }

    private final class MSBDataReader implements DataReader
    {
        @Override
        public float readData(final DataInputStream dataInputStream) throws IOException
        {
            return dataInputStream.readFloat();
        }
    }

    public RasterPlugin()
    {
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll((Listener) this);
    }

    @Override
    public void onEnable()
    {
        getCommand("pos").setExecutor(new PosCommand(this));
        getCommand("label").setExecutor(new LabelCommand(this));
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(
            final String worldName,
            final String args)
    {
        ChunkGenerator chunkGenerator = null;
        if (args.endsWith(".flt"))
        {
            getLogger().info("----> getDefaultWorldGenerator::" + worldName + ":" + args);
            final RasterInfo rasterInfo = new RasterInfo();
            loadRaster(rasterInfo, args);
            if (worldName.equals("cntry"))
            {
                chunkGenerator = new CntryGenerator(rasterInfo, 50);
            }
            else
            {
                chunkGenerator = new RasterGenerator(rasterInfo, 50);
            }
            rasterInfoMap.put(worldName, rasterInfo);
        }
        return chunkGenerator;
    }

    private void loadRaster(
            final RasterInfo rasterInfo,
            final String path)
    {
        try
        {
            loadRasterHeader(rasterInfo, path);
            loadRasterData(rasterInfo, path);
        }
        catch (IOException e)
        {
            getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    private void loadRasterData(
            final RasterInfo rasterInfo,
            final String path) throws IOException
    {
        final DataInputStream dataInputStream = new DataInputStream(new FileInputStream(path));
        try
        {
            final DataReader dataReader;
            if (rasterInfo.isLSBFirst)
            {
                dataReader = new LSBDataReader();
            }
            else
            {
                dataReader = new MSBDataReader();
            }
            int index = 0;
            int maxr = 0;
            int maxc = 0;
            float minData = Float.POSITIVE_INFINITY;
            float maxData = Float.NEGATIVE_INFINITY;
            float[] data = new float[rasterInfo.nrows * rasterInfo.ncols];
            for (int r = 0; r < rasterInfo.nrows; r++)
            {
                for (int c = 0; c < rasterInfo.ncols; c++)
                {
                    final float datum = dataReader.readData(dataInputStream);
                    data[index++] = datum;
                    if (datum != rasterInfo.nodata)
                    {
                        minData = Math.min(minData, datum);
                        if (datum > maxData)
                        {
                            maxData = datum;
                            maxr = r;
                            maxc = c;
                        }
                    }
                }
            }
            rasterInfo.maxr = maxr;
            rasterInfo.maxc = maxc;
            rasterInfo.minData = minData;
            rasterInfo.maxData = maxData;
            rasterInfo.delData = maxData - minData;
            rasterInfo.data = data;
            // getLogger().info(String.format("---> min=%.3f max=%.3f del=%.3f", minData, maxData, rasterInfo.delData));
            // getLogger().info(String.format("maxr=%d maxc=%d", maxr, maxc));
        }
        finally
        {
            dataInputStream.close();
        }
    }

    private void loadRasterHeader(
            final RasterInfo rasterInfo,
            final String fltPath) throws IOException
    {
        final String hdrPath = fltPath.replace(".flt", ".hdr");
        final LineNumberReader lineReader = new LineNumberReader(new FileReader(hdrPath));
        try
        {
            final Pattern pattern = Pattern.compile("\\s+");
            String line = lineReader.readLine();
            while (line != null)
            {
                final String[] tokens = pattern.split(line);
                if (tokens.length == 2)
                {
                    final String key = tokens[0];
                    final String val = tokens[1];
                    if ("xllcorner".equalsIgnoreCase(key))
                    {
                        rasterInfo.minx = Double.parseDouble(val);
                    }
                    else if ("yllcorner".equalsIgnoreCase(key))
                    {
                        rasterInfo.miny = Double.parseDouble(val);
                    }
                    else if ("cellsize".equalsIgnoreCase(key))
                    {
                        rasterInfo.cellSize = Double.parseDouble(val);
                    }
                    else if ("ncols".equalsIgnoreCase(key))
                    {
                        rasterInfo.ncols = Integer.parseInt(val);
                    }
                    else if ("nrows".equalsIgnoreCase(key))
                    {
                        rasterInfo.nrows = Integer.parseInt(val);
                    }
                    else if ("nodata_value".equalsIgnoreCase(key))
                    {
                        rasterInfo.nodata = Float.parseFloat(val);
                    }
                    else if ("byteorder".equalsIgnoreCase(key))
                    {
                        rasterInfo.isLSBFirst = "lsbfirst".equalsIgnoreCase(val);
                    }
                }
                line = lineReader.readLine();
            }
        }
        finally
        {
            lineReader.close();
        }
    }
}
