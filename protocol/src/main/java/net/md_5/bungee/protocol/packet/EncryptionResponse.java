package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EncryptionResponse extends DefinedPacket
{

    private byte[] sharedSecret;
    private byte[] verifyToken;
    private EncryptionData encryptionData;

    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_8 )
        {
            sharedSecret = readArrayLegacy( buf );
            verifyToken = readArrayLegacy( buf );
        } else
        {
            sharedSecret = readArray( buf, 128 );
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_19 || buf.readBoolean() )
            {
                verifyToken = readArray( buf, 128 );
            } else
            {
                encryptionData = new EncryptionData( buf.readLong(), readArray( buf ) );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_8 )
        {
            writeArrayLegacy( sharedSecret, buf, false );
            writeArrayLegacy( verifyToken, buf, false );
        } else
        {
            writeArray( sharedSecret, buf );
            if ( verifyToken != null )
            {
                if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
                {
                    buf.writeBoolean( true );
                }
                writeArray( verifyToken, buf );
            } else
            {
                buf.writeLong( encryptionData.getSalt() );
                writeArray( encryptionData.getSignature(), buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Data
    public static class EncryptionData
    {
        private final long salt;
        private final byte[] signature;
    }
}
