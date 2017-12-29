package com.freeipodsoftware.abc;

public class TagSuggestionStrategy implements EventListener {
    private TagEditor tagEditor;
    private InputFileSelection inputFileSelection;
    private String firstInputFileName;

    public TagSuggestionStrategy() {
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        eventDispatcher.addListener(this);
    }

    public void setTagEditor(TagEditor tagEditor) {
        this.tagEditor = tagEditor;
    }

    public void setInputFileSelection(InputFileSelection inputFileSelection) {
        this.inputFileSelection = inputFileSelection;
    }

    public void onEvent(String eventId) {
        if (eventId.equals("fileListChangedEvent")) {
            this.suggestTags();
        }

    }

    private void suggestTags() {
        if (this.inputFileSelection.getFileList().length > 0 && !this.inputFileSelection.getFileList()[0].equals(this.firstInputFileName)) {
            this.firstInputFileName = this.inputFileSelection.getFileList()[0];
            this.tagEditor.clear();
            Mp4Tags tags = Util.readTagsFromInputFile(this.firstInputFileName);
            this.tagEditor.setArtist(tags.getWriter());
            this.tagEditor.setTitle(tags.getTitle());
            this.tagEditor.setAlbum(tags.getSeries());
            this.tagEditor.setGenre(tags.getGenre());
            this.tagEditor.setYear(tags.getYear());
            if (tags.getTrack() > 0) {
                this.tagEditor.setTrack(String.valueOf(tags.getTrack()));
            }
            if (tags.getTotalTracks() > 0) {
                this.tagEditor.setTotalTracks(String.valueOf(tags.getTotalTracks()));
            }
            this.tagEditor.setComment(tags.getComment());
        }

    }
}