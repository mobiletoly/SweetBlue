package com.idevicesinc.sweetblue.utils;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceConfig;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleDevice.StateListener;
import com.idevicesinc.sweetblue.BleManager.DiscoveryListener;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.BleState;
import com.idevicesinc.sweetblue.BleManagerConfig.AdvertisingFilter;
import com.idevicesinc.sweetblue.BleManagerConfig.AdvertisingFilter.Packet;

/**
 * Bitwise enum contract for representing the state of devices and managers.
 * Implementations are {@link BleDeviceState} and {@link BleState}.
 * Not intended for subclassing outside this library but go wild if you want.
 */
public interface State
{
	/**
	 * Abstract base class for {@link BleDevice.StateListener.ChangeEvent} and {@link BleManager.StateListener.ChangeEvent}.
	 */
	public static abstract class ChangeEvent
	{
		/**
		 * The bitwise representation of the {@link BleDevice} or {@link BleManager}
		 * before the event took place.
		 */
		public final int oldStateBits;
		
		/**
		 * The new and now current bitwise representation of a {@link BleDevice}
		 * or {@link BleManager}. Will be the same as {@link BleDevice#getStateMask()}
		 * or {@link BleManager#getStateMask()}.
		 */
		public final int newStateBits;
		
		/**
		 * For each old->new bit difference, this mask will tell you if the transition was intentional. Intentional generally means a call was made to
		 * a public method of the library from app-code to trigger the state change, and so usually the stacktrace started from a user input event upstream.
		 * Otherwise the given bit will be 0x0 and so the state change was "unintentional". An example of intentional is if you call
		 * {@link BleDevice#disconnect()} in response to a button click, whereas unintentional would be if the device disconnected because it
		 * went out of range. As much as possible these flags are meant to represent the actual app <i>user's</i> intent through the app, not
		 * the intent of you the programmer, nor the intent of the user outside the bounds of the app, like disconnecting by turning the peripheral off.
		 * For example after a disconnect you might be using {@link BleManagerConfig#reconnectRateLimiter} to try periodically
		 * reconnecting. From you the programmer's perspective a connect, if/when it happens, is arguably an intentional action. From the user's
		 * perspective however the connect was unintentional. Therefore this mask is currently meant to serve an analytics or debugging role,
		 * not to necessarily gate application logic.
		 */
		public final int intentMask;
		
		protected ChangeEvent(int oldStateBits_in, int newStateBits_in, int intentMask_in)
		{
			this.oldStateBits = oldStateBits_in;
			this.newStateBits = newStateBits_in;
			this.intentMask = intentMask_in;
		}
		
		/**
		 * Convenience forwarding of {@link State#wasEntered(int, int)}.
		 */
		public boolean wasEntered(State state)
		{
			return state.wasEntered(oldStateBits, newStateBits);
		}
		
		/**
		 * Convenience forwarding of {@link State#wasExited(int, int)}.
		 */
		public boolean wasExited(State state)
		{
			return state.wasExited(oldStateBits, newStateBits);
		}
		
		/**
		 * Returns the intention behind the state change, or {@link ChangeIntent#NULL} if no state
		 * change for the given state occured.
		 */
		public ChangeIntent getIntent(State state)
		{
			if( (state.bit() & oldStateBits) == (state.bit() & newStateBits) )
			{
				return ChangeIntent.NULL;
			}
			else
			{
				return state.overlaps(intentMask) ? ChangeIntent.INTENTIONAL : ChangeIntent.UNINTENTIONAL;
			}
		}
	}
	
	/**
	 * Enumerates the intention behind a single state change - as comprehensively as possible, whether the
	 * application user intended for the state change to happen or not. See {@link ChangeEvent#intentMask} for more
	 * discussion on user intent.
	 */
	public static enum ChangeIntent
	{
		/**
		 * Used instead of Java's built-in <code>null</code> wherever appropriate.
		 */
		NULL,
		
		/**
		 * The state change was not intentional.
		 */
		UNINTENTIONAL,
		
		/**
		 * The state change was intentional.
		 */
		INTENTIONAL;
		
		private static final int DISK_VALUE__NULL				= -1;
		private static final int DISK_VALUE__UNINTENTIONAL		=  0;
		private static final int DISK_VALUE__INTENTIONAL		=  1;
		
		/**
		 * The integer value to write to disk. Not using ordinal to avoid
		 * unintentional consequences of changing enum order by accident or something.
		 */
		public int toDiskValue()
		{
			switch(this)
			{
				case INTENTIONAL:		return DISK_VALUE__INTENTIONAL;
				case UNINTENTIONAL:		return DISK_VALUE__UNINTENTIONAL;
				case NULL:
				default:				return DISK_VALUE__NULL;
			}
		}
		
		/**
		 * Transforms {@link #toDiskValue()} back to the enum.
		 * Returns {@link #NULL} if diskValue can't be resolved.
		 */
		public static ChangeIntent fromDiskValue(int diskValue)
		{
			for( int i = 0; i < values().length; i++ )
			{
				if( values()[i].toDiskValue() == diskValue )
				{
					return values()[i];
				}
			}
			
			return NULL;
		}
	}
	
	/**
	 * Returns the bit (0x1, 0x2, 0x4, etc.) this enum represents based on the {@link #ordinal()}.
	 */
	int bit();
	
	/**
	 * Convenience method for checking if <code>({@link #bit()} & mask) != 0x0</code>.
	 */
	boolean overlaps(int mask);
	
	/**
	 * Same as {@link Enum#ordinal()}.
	 */
	int ordinal();
	
	/**
	 * Same as {@link Enum#name()}.
	 */
	String name();
	
	/**
	 * Given an old and new state mask from {@link StateListener#onStateChange(BleDevice, int, int)}, this
	 * method tells you whether the 'this' state was appended.
	 * 
	 * @see #wasExited(int, int)
	 */
	boolean wasEntered(int oldStateBits, int newStateBits);
	
	/**
	 * Reverse of {@link #wasEntered(int, int)}.
	 * 
	 * @see #wasEntered(int, int)
	 */
	boolean wasExited(int oldStateBits, int newStateBits);
}