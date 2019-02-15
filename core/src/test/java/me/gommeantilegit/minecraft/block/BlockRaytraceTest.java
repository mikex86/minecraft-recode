package me.gommeantilegit.minecraft.block;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BlockRaytraceTest {

    @Test
    public void collisionRayTrace() {
        Block testBlock = new Block("TestBlock", 1, new String[]{
        }, true);

        BlockPos position = new BlockPos(10, 10, 13);
        Vector3 rayStart = new Vector3(10, 10, 10);
        Vector3 direction = new Vector3(0, 0, 1);
        Vector3 rayEnd = new Vector3(10, 10, 10).add(direction.cpy().scl(4));

        RayTracer.RayTraceResult result = testBlock.collisionRayTrace(position, rayStart, direction, 4);
        assertNotNull(result);
    }
}