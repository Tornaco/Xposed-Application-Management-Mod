package dev.nick.tiles.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewAnimateUtils {

    public static final String COLOR_PROPERTY = "color";
    public static final int DURATION_SHORT = 300;
    public static final int DURATION_MID = 800;

    public static void animateColorChange(View view, int fromColor, int toColor, int duration,
                                          Animator.AnimatorListener listener) {
        if (view.getWindowToken() == null) {
            return;
        }
        AnimatorSet animation = new AnimatorSet();
        ObjectAnimator colorAnimator = ObjectAnimator.ofInt(view, COLOR_PROPERTY, fromColor, toColor);
        colorAnimator.setEvaluator(new ArgbEvaluator());
        colorAnimator.setDuration(duration);
        if (listener != null)
            animation.addListener(listener);
        animation.play(colorAnimator);
        animation.start();
    }

    public static void circularHide(final View view, Animator.AnimatorListener listener) {
        Animator anim = createCircularHideAnimator(view, listener);
        if (anim != null)
            anim.start();
    }

    public static Animator createCircularHideAnimator(final View view,
                                                      @Nullable Animator.AnimatorListener listener) {
        if (view.getWindowToken() == null
                || view.getVisibility() == View.INVISIBLE)
            return null;

        // get the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        // get the initial radius for the clipping circle
        int initialRadius = view.getWidth();

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);

        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        });

        if (listener != null) {
            anim.addListener(listener);
        }
        return anim;
    }

    public static void circularShow(final View view) {
        Animator anim = createCircularShowAnimator(view);
        if (anim != null)
            anim.start();
    }

    public static Animator createCircularShowAnimator(final View view) {
        if (view.getVisibility() == View.VISIBLE
                || view.getWindowToken() == null)
            return null;
        // get the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        // get the final radius for the clipping circle
        int finalRadius = view.getWidth();

        // create and start the animator for this view
        // (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }
        });
        return anim;
    }

    public static void alphaShow(@NonNull final View view) {
        if (view.getWindowToken() == null)
            return;
        view.setVisibility(View.VISIBLE);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alpha.setDuration(DURATION_MID);
        alpha.start();
    }

    public static void alphaHide(@NonNull final View view, final Runnable rWhenDone) {
        if (view.getWindowToken() == null) {
            if (rWhenDone != null)
                rWhenDone.run();
            return;
        }
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        alpha.setDuration(DURATION_MID);
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                if (rWhenDone != null)
                    rWhenDone.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                if (rWhenDone != null)
                    rWhenDone.run();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alpha.start();
    }

    public static void animateTextChange(final TextView view, @IdRes final int toText,
                                         final Runnable rWhenEnd) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        final ObjectAnimator restore = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alpha.setDuration(DURATION_SHORT);
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());
        restore.setDuration(DURATION_SHORT);
        restore.setInterpolator(new AccelerateDecelerateInterpolator());
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Do nothing.
            }

            @SuppressWarnings("ResourceType")
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setText(toText);
                restore.start();
            }

            @SuppressWarnings("ResourceType")
            @Override
            public void onAnimationCancel(Animator animation) {
                view.setText(toText);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Do nothing.
            }
        });
        if (rWhenEnd != null)
            restore.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    rWhenEnd.run();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    rWhenEnd.run();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        alpha.start();
    }

    public static void animateTextChange(final TextView view, final String toText) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        final ObjectAnimator restore = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        alpha.setDuration(DURATION_SHORT);
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());
        restore.setDuration(DURATION_SHORT);
        restore.setInterpolator(new AccelerateDecelerateInterpolator());
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Do nothing.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setText(toText);
                restore.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setText(toText);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Do nothing.
            }
        });
        alpha.start();
    }


    public static void scaleShow(final View view) {
        scaleShow(view, null);
    }

    public static void scaleShow(final View view, final Runnable rWhenEnd) {
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            if (rWhenEnd != null) {
                rWhenEnd.run();
            }
            return;
        }
        if (view.getWindowToken() == null) {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    scaleShow(view);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            });
        }
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(DURATION_SHORT);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.VISIBLE);
                if (rWhenEnd != null) {
                    rWhenEnd.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setVisibility(View.VISIBLE);
                if (rWhenEnd != null) {
                    rWhenEnd.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }


    public static void scaleHide(final View view) {
        scaleHide(view, null);
    }

    public static void scaleHide(final View view, final Runnable rWhenEnd) {
        if (view.getWindowToken() == null) {
            view.setVisibility(View.INVISIBLE);
            if (rWhenEnd != null) {
                rWhenEnd.run();
            }
            return;
        }
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(DURATION_SHORT);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                if (rWhenEnd != null) {
                    rWhenEnd.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                if (rWhenEnd != null) {
                    rWhenEnd.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    public static void runFlipHorizonAnimation(@NonNull View view, long duration, final Runnable rWhenEnd) {
        view.setAlpha(0);
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view,
                "rotationY", -180f, 0f);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "alpha",
                0f, 1f);
        set.setDuration(duration);
        set.playTogether(objectAnimator1, objectAnimator2);
        if (rWhenEnd != null)
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    rWhenEnd.run();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        set.start();
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant        The descendant to which the passed coordinate is relative.
     * @param root              The root view to make the coordinates relative to.
     * @param coord             The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *                          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     * this scale factor is assumed to be equal in X and Y, and so if at any point this
     * assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(View descendant, View root,
                                                           int[] coord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while (v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }

            v0.getMatrix().mapPoints(pt);
            pt[0] += v0.getLeft();
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }
}
