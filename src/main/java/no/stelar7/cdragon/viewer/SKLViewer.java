package no.stelar7.cdragon.viewer;

import no.stelar7.cdragon.types.dds.DDSParser;
import no.stelar7.cdragon.types.skn.SKNParser;
import no.stelar7.cdragon.types.skn.data.SKNFile;
import no.stelar7.cdragon.util.handlers.UtilHandler;
import no.stelar7.cdragon.viewer.rendering.Renderer;
import no.stelar7.cdragon.viewer.rendering.models.*;
import no.stelar7.cdragon.viewer.rendering.shaders.*;
import org.joml.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Math;
import java.nio.file.*;

import static org.lwjgl.opengl.GL11.*;

public class SKLViewer extends Renderer
{
    public SKLViewer(int width, int height)
    {
        super(width, height);
    }
    
    public static void main(String[] args)
    {
        new SKLViewer(600, 600).start();
    }
    
    @Override
    public void initPostGL()
    {
        try
        {
            Files.deleteIfExists(Paths.get("C:\\Users\\Steffen\\Downloads").resolve("gl.log"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }

//        Path path = UtilHandler.DOWNLOADS_FOLDER.resolve("parser_test\\Brand");
//        model = new Model(path, "Brand_frostfire.skn", "brand_frostfire_TX_CM.dds");
        
        Path    path = UtilHandler.DOWNLOADS_FOLDER.resolve("parser_test\\Caitlyn");
        SKNFile skn  = new SKNParser().parse(path.resolve("Caitlyn_cop.skn"));
        
        Mesh mesh = new Mesh(skn);
        
        BufferedImage texImg = new DDSParser().parse(path.resolve("caitlyn_cop_TX_CM.dds"));
        Texture       tex    = new Texture(skn, texImg);
        
        model = new Model(mesh, tex);
        
        Shader vert = new Shader("shaders/basic.vert");
        Shader frag = new Shader("shaders/basic.frag");
        
        prog = new Program();
        prog.attach(vert);
        prog.attach(frag);
        
        prog.bindVertLocation("position", 0);
        prog.bindVertLocation("uv", 1);
        
        prog.bindFragLocation("color", 0);
        
        prog.link();
    }
    
    float x;
    float z;
    float   time  = 0;
    boolean dirty = true;
    
    private void updateMVP()
    {
        if (!dirty)
        {
            return;
        }
        
        
        float perspective = (float) width / (float) height;
        float fov         = (float) Math.toRadians(65);
        
        Matrix4f projection = new Matrix4f().setPerspective(fov, perspective, 0.1f, 10000f);
        
        Matrix4f view = new Matrix4f().setLookAt(
                new Vector3f(x, 0.5f, z),
                new Vector3f(0, 0, 0),
                new Vector3f(0, 1f, 0));
        
        Matrix4f model = new Matrix4f().scaling(2);
        
        Matrix4f mvp = projection.mul(view, new Matrix4f()).mul(model, new Matrix4f());
        
        prog.bind();
        prog.setMatrix4f("mvp", mvp);
        prog.setInt("texImg", 0);
        dirty = false;
    }
    
    @Override
    public void update()
    {
        time += .01f;
        
        x = (float) Math.sin(time) * 10;
        z = (float) Math.cos(time) * 10;
        
        updateMVP();
    }
    
    float last = 0;
    
    Model   model;
    Program prog;
    
    @Override
    public void render()
    {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        UtilHandler.logToFile("gl.log", "glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)");
        
        if (prog != Program.current)
        {
            Program.current = prog;
            prog.bind();
        }
        
        
        if (model != Model.current)
        {
            Model.current = model;
            model.bind();
        }
        
        glDrawElements(GL_TRIANGLES, model.getMesh().getIndexCount(), GL_UNSIGNED_INT, 0);
        UtilHandler.logToFile("gl.log", String.format("glDrawElements(GL_TRIANGLES, %s, GL_UNSIGNED_INT, 0)", model.getMesh().getIndexCount()));
    }
}
