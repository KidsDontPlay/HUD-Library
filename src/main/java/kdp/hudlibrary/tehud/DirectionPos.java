package kdp.hudlibrary.tehud;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class DirectionPos {

    public final BlockPos pos;
    public final Direction face;

    private static Cache<BlockPos, Cache<Direction, DirectionPos>> cache = CacheBuilder.newBuilder().maximumSize(100)
            .build();

    public static DirectionPos of(BlockPos pos, Direction face) {
        try {
            return cache.get(pos, () -> CacheBuilder.newBuilder().build()).get(face, () -> new DirectionPos(pos, face));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private DirectionPos(BlockPos pos, Direction face) {
        super();
        this.pos = pos;
        this.face = face;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((face == null) ? 0 : face.hashCode());
        result = prime * result + ((pos == null) ? 0 : pos.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DirectionPos other = (DirectionPos) obj;
        if (face != other.face)
            return false;
        if (pos == null) {
            if (other.pos != null)
                return false;
        } else if (!pos.equals(other.pos))
            return false;
        return true;
    }

}
