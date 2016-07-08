package com.thexfactor117.levels.events;

import com.thexfactor117.levels.Levels;
import com.thexfactor117.levels.capabilities.CapabilityEnemyLevel;
import com.thexfactor117.levels.capabilities.IEnemyLevel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author TheXFactor117
 * @author AtomicStryker
 *
 */
public class EventRenderOverlay 
{
	private final double NAME_VISION_DISTANCE = 32D;
	
	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Text event)
	{
		Entity entity = getEntityCrosshairOver(event.getPartialTicks(), FMLClientHandler.instance().getClient());
		
		if (entity != null && entity instanceof EntityMob)
		{
			if (entity.worldObj.isRemote)
			{
				EntityMob mob = (EntityMob) entity;
				
				if (mob.hasCapability(CapabilityEnemyLevel.ENEMY_LEVEL_CAP, null))
				{
					IEnemyLevel enemyLevel = mob.getCapability(CapabilityEnemyLevel.ENEMY_LEVEL_CAP, null);
					Levels.LOGGER.info("Level from Render: " + enemyLevel.getEnemyLevel());
					String buffer = "Default";
					if (enemyLevel.getEnemyLevel() == 1) buffer = "Weakened";
					if (enemyLevel.getEnemyLevel() == 2) buffer = "Normal";
					if (enemyLevel.getEnemyLevel() == 3) buffer = "Hardened";
					if (enemyLevel.getEnemyLevel() == 4) buffer = "Superior";
					if (enemyLevel.getEnemyLevel() == 5) buffer = "Elite";
					if (enemyLevel.getEnemyLevel() == 6) buffer = "Legendary";
					ScaledResolution resolution = new ScaledResolution(FMLClientHandler.instance().getClient());
	                int screenwidth = resolution.getScaledWidth();
	                FontRenderer fontR = FMLClientHandler.instance().getClient().fontRendererObj;
	                
	                fontR.drawString(buffer, screenwidth / 2, 10, 0xffffff);
				}
			}
		}
	}
	
	private Entity getEntityCrosshairOver(float renderTick, Minecraft mc)
    {
        Entity returnedEntity = null;

        if (mc.getRenderViewEntity() != null)
        {
            if (mc.theWorld != null)
            {
                double reachDistance = NAME_VISION_DISTANCE;
                final RayTraceResult mopos = mc.getRenderViewEntity().rayTrace(reachDistance, renderTick);
                double reachDist2 = reachDistance;
                final Vec3d viewEntPositionVec = mc.getRenderViewEntity().getPositionVector();

                if (mopos != null)
                {
                    reachDist2 = mopos.hitVec.squareDistanceTo(viewEntPositionVec);
                }

                final Vec3d viewEntityLookVec = mc.getRenderViewEntity().getLook(renderTick);
                final Vec3d actualReachVector = viewEntPositionVec.addVector(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance, viewEntityLookVec.zCoord * reachDistance);
                float expandBBvalue = 0.0F;
                double lowestDistance = reachDist2;
                Entity iterEnt;
                Entity pointedEntity = null;
                for (Object obj : mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(viewEntityLookVec.xCoord * reachDistance, viewEntityLookVec.yCoord * reachDistance, viewEntityLookVec.zCoord * reachDistance).expand((double) expandBBvalue, (double) expandBBvalue, (double) expandBBvalue)))
                {
                    iterEnt = (Entity) obj;
                    if (iterEnt.canBeCollidedWith())
                    {
                        float entBorderSize = iterEnt.getCollisionBorderSize();
                        AxisAlignedBB entHitBox = iterEnt.getEntityBoundingBox().expand(entBorderSize, entBorderSize + 1.5, entBorderSize).setMaxY(iterEnt.getEntityBoundingBox().maxY - 1.5);
                        RayTraceResult interceptObjectPosition = entHitBox.calculateIntercept(viewEntPositionVec, actualReachVector);

                        if (entHitBox.isVecInside(viewEntPositionVec))
                        {
                            if (0.0D < lowestDistance || lowestDistance == 0.0D)
                            {
                                pointedEntity = iterEnt;
                                lowestDistance = 0.0D;
                            }
                        }
                        else if (interceptObjectPosition != null)
                        {
                            double distanceToEnt = viewEntPositionVec.distanceTo(interceptObjectPosition.hitVec);

                            if (distanceToEnt < lowestDistance || lowestDistance == 0.0D)
                            {
                                pointedEntity = iterEnt;
                                lowestDistance = distanceToEnt;
                            }
                        }
                    }
                }

                if (pointedEntity != null && (lowestDistance < reachDist2 || mopos == null))
                {
                    returnedEntity = pointedEntity;
                }
            }
        }

        return returnedEntity;
    }
}
