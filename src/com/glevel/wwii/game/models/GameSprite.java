package com.glevel.wwii.game.models;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.glevel.wwii.game.GraphicsFactory;
import com.glevel.wwii.game.InputManager;
import com.glevel.wwii.game.graphics.CenteredSprite;

public class GameSprite extends CenteredSprite {

    private final GameElement mGameElement;
    private static final int ACTION_MOVE_THRESHOLD = 70;
    private static final int VALID_ORDER_THRESHOLD = 50;
    private InputManager mInputManager;
    private boolean mIsGrabbed = false;
    private boolean mIsSelected = false;
    private boolean wasSelected = false;

    private Sprite specialSprite;
    public boolean isFiring;
    private boolean canBeDragged;

    public GameSprite(GameElement gameElement, InputManager inputManager, float pX, float pY,
            ITextureRegion pTextureRegion, VertexBufferObjectManager mVertexBufferObjectManager) {
        super(pX, pY, pTextureRegion, mVertexBufferObjectManager);
        mGameElement = gameElement;
        mInputManager = inputManager;
        this.setScale(mGameElement.getSpriteScale());
        addMuzzleFlashSprite();
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX,
            final float pTouchAreaLocalY) {
        switch (pSceneTouchEvent.getAction()) {
        case TouchEvent.ACTION_DOWN:
            // element is selected
            wasSelected = mInputManager.selectedElement != null;
            mInputManager.onSelectGameElement(this);
            this.setAlpha(0.8f);
            mIsSelected = true;
            break;
        case TouchEvent.ACTION_MOVE:
            if (canBeDragged
                    && mIsSelected
                    && !mIsGrabbed
                    && (Math.abs(pTouchAreaLocalX) + Math.abs(pTouchAreaLocalY) > ACTION_MOVE_THRESHOLD || mInputManager
                            .isDeploymentPhase())) {
                // element is dragged
                mIsGrabbed = true;
            }
            if (mIsSelected && mIsGrabbed) {
                mInputManager.updateOrderLine(this, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
            }
            break;
        case TouchEvent.ACTION_UP:
            if (mIsGrabbed) {
                // cancel if small distance
                if (Math.abs(pSceneTouchEvent.getX() - getX()) + Math.abs(pSceneTouchEvent.getY() - getY()) > VALID_ORDER_THRESHOLD) {
                    // give order to unit
                    mInputManager.giveOrderToUnit(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                }
            } else if (wasSelected
                    && Math.abs(pSceneTouchEvent.getX() - getX()) + Math.abs(pSceneTouchEvent.getY() - getY()) < VALID_ORDER_THRESHOLD) {
                mInputManager.giveHideOrder(this);
            }
            mInputManager.hideOrderLine();
            mIsGrabbed = false;
            if (mIsSelected) {
                this.setAlpha(1.0f);
                mIsSelected = false;
            }
            break;
        }
        return true;
    }

    public GameElement getGameElement() {
        return mGameElement;
    }

    public void addMuzzleFlashSprite() {
        specialSprite = new Sprite(45, -60, GraphicsFactory.mGfxMap.get("muzzle_flash.png"),
                getVertexBufferObjectManager());
        specialSprite.setVisible(false);
        attachChild(specialSprite);
    }

    @Override
    protected void onManagedUpdate(float pSecondsElapsed) {
        super.onManagedUpdate(pSecondsElapsed);
        if (isFiring) {
            specialSprite.setVisible(true);
            isFiring = false;
        } else if (specialSprite.isVisible()) {
            specialSprite.setVisible(false);
        }
    }

    public void setCanBeDragged(boolean canBeDragged) {
        this.canBeDragged = canBeDragged;
    }

}
