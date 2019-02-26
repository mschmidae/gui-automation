package io.github.mschmidae.guiautomation.run;

import io.github.mschmidae.guiautomation.control.clipboard.Clipboard;
import io.github.mschmidae.guiautomation.control.keyboard.Key;
import io.github.mschmidae.guiautomation.control.keyboard.Keyboard;
import io.github.mschmidae.guiautomation.control.keyboard.Shortcut;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class Centos7XfceIntegrationTest {
  private static final int SLEEP_TIME = 1_000;

  @Test
  void openNotepadTypeTextCopyToClipboard() throws InterruptedException {
    Keyboard keyboard = new Keyboard();
    Clipboard clipboard = new Clipboard();

    openNotepad(keyboard);

    keyboard.press(Key.SHIFT).input(Key.NUM_8).release(Key.SHIFT)
        .input(Key.A).press(Key.SHIFT).input(Key.A).release(Key.SHIFT)
        .input(Key.B).press(Key.SHIFT).input(Key.B).release(Key.SHIFT)
        .press(Key.SHIFT).input(Key.NUM_9).release(Key.SHIFT);

    Thread.sleep(SLEEP_TIME);
    keyboard.execute(Shortcut.MARK_ALL).execute(Shortcut.COPY);

    closeNotepad(keyboard);

    assertThat(clipboard.get()).isPresent().contains("(aAbB)");
  }

  @Test
  void openNotepadInsertFromClipboardModifyTextCopyToClipboard() throws InterruptedException {
    Keyboard keyboard = new Keyboard();
    Clipboard clipboard = new Clipboard();

    openNotepad(keyboard);

    clipboard.set("TEST");
    keyboard.execute(Shortcut.PASTE);

    Thread.sleep(SLEEP_TIME);
    keyboard.input(Key.HOME).input(Key.BEGIN).press(Key.SHIFT).input(Key.NUM_8).release(Key.SHIFT);
    keyboard.input(Key.END).press(Key.SHIFT).input(Key.NUM_9).release(Key.SHIFT);

    Thread.sleep(SLEEP_TIME);
    keyboard.execute(Shortcut.MARK_ALL).execute(Shortcut.COPY);

    closeNotepad(keyboard);

    assertThat(clipboard.get()).isPresent().contains("(TEST)");
  }

  private void openTerminal(final Keyboard keyboard) throws InterruptedException {
    keyboard.press(Key.ALT).input(Key.F3).release(Key.ALT);
    Thread.sleep(SLEEP_TIME);

    keyboard.type("xfce-terminal").input(Key.DOWN).input(Key.DOWN).input(Key.ENTER);
    Thread.sleep(SLEEP_TIME);
  }

  private void closeTerminal(final Keyboard keyboard) {
    keyboard.press(Key.CONTROL).input(Key.D).release(Key.CONTROL);
  }

  private void openNotepad(final Keyboard keyboard) throws InterruptedException {
    openTerminal(keyboard);
    keyboard.type("gedit").input(Key.ENTER);
    Thread.sleep(SLEEP_TIME);
  }

  private void closeNotepad(final Keyboard keyboard) throws InterruptedException {
    keyboard.execute(Shortcut.CLOSE);
    Thread.sleep(SLEEP_TIME);
    keyboard.input(Key.TAB).input(Key.ENTER);

    Thread.sleep(SLEEP_TIME);
    closeTerminal(keyboard);
  }
}
