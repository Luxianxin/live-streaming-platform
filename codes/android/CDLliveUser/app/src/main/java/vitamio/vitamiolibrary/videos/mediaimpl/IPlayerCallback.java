package vitamio.vitamiolibrary.videos.mediaimpl;




public abstract interface IPlayerCallback {

	public abstract void onBufferingEnd();

	public abstract void onBufferingStart();
	
	public abstract void onTackLagging();

	public abstract void onBufferingback(int paramInt);

	public abstract void onCompletePlayback();


	public abstract void onPreparedPlayback();

	public abstract void onSetVideoViewLayout();

	public abstract void onSurfaceCreated(boolean paramBoolean);
	
	
}
