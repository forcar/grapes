/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.jnp.grapes.io;

import java.nio.file.Path;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.AbstractEventWriterService;
import org.jlab.clara.std.services.EventWriterException;
import org.jlab.jnp.hipo.data.DataFrame;
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.io.HipoWriterStream;
import org.jlab.jnp.utils.file.FileUtils;
import org.json.JSONObject;

/**
 *
 * @author gavalian
 */
public class HipoFrameWriter extends AbstractEventWriterService<HipoWriterStream> {
    
    private static final String CONF_COMPRESSION = "compression";
    private static final String CONF_SCHEMA = "schema_dir";
    
    @Override
    protected HipoWriterStream createWriter(Path file, JSONObject opts) throws EventWriterException {
        try {
            HipoWriterStream writer = new HipoWriterStream();
            int compression = getCompression(opts);
            System.out.printf("%s service: using compression level %d%n", getName(), compression);
            //writer.setCompressionType(compression);
            //writer.getSche.initFromDirectory(getSchemaDirectory(opts));
            //writer.open(file.toString());
            writer.getSchemaFactory().initFromDirectory(getSchemaDirectory(opts));
            //for(int i = 0; i < 5; i++){
            //    writer.addWriter(i, file.toString());
            //}            
            writer.setFileName(file.toString());
            writer.open();
            return writer;
        } catch (Exception e) {
            throw new EventWriterException(e);
        }
    }

    private int getCompression(JSONObject opts) {
        return opts.has(CONF_COMPRESSION) ? opts.getInt(CONF_COMPRESSION) : 2;
    }

    private String getSchemaDirectory(JSONObject opts) {
        return opts.has(CONF_SCHEMA)
                ? opts.getString(CONF_SCHEMA)
                : FileUtils.getEnvironmentPath("CLAS12DIR", "etc/bankdefs/hipo");
    }


    @Override
    protected void closeWriter() {
        writer.close();
    }

    @Override
    protected void writeEvent(Object event) throws EventWriterException {
        try {
            DataFrame  dataFrame = (DataFrame) event;
            
            int count = dataFrame.getCount();
            for(int i = 0; i < count; i++){
                byte[] buffer = dataFrame.getEntry(i);
                HipoEvent  hipoEvent = new HipoEvent(buffer);
                for(int k = 0; k < 32; k++){
                    int status = hipoEvent.getEventStatusBit(k);
                    if(status>0) {                 
                        writer.writeEvent(k,hipoEvent);
                    }
                }
            }
           /* HipoEvent hipo_event = (HipoEvent) event;
            for(int i = 0; i < 5; i++){
                //int bitStatus = hipo_event.getEventStatus();
                //System.out.println("[writer] event status = " + bitStatus);
                int status = hipo_event.getEventStatusBit(i);
                if(status>0) {
                    //System.out.println("-----> writing event with status # " + status);
                    writer.writeEvent(i,hipo_event);
                }
            }*/
        } catch (Exception e) {
            throw new EventWriterException(e);
        }
    }

    @Override
    protected EngineDataType getDataType() {
        return Clas12Types.HIPOFRAME;
    }
}
