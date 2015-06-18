#include <pebble.h>
#include "bus_layout.h"
  
// Main Loop
int main(void) {
  //init();
  show_bus_layout();
  app_event_loop();
  hide_bus_layout();
  //deinit();
  return 0;
}

//static Window *window;
//static TextLayer *text_layer;

  /*
enum {
    KEY_BUTTON_EVENT = 0,
    BUTTON_PREVIOUS = 1,
    BUTTON_NEXT = 2,
    BUTTON_REFRESH = 3,
    MESSAGE_DATA = 4,
    MESSAGE_DATA_EVENT = 5
};


// App Message API
static void in_received_handler(DictionaryIterator *iter, void *context){
  //TODO Handle incoming messages
}

// Send command
void send_int(uint8_t key, uint8_t cmd)
{
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
      
    Tuplet value = TupletInteger(key, cmd);
    dict_write_tuplet(iter, &value);
      
    app_message_outbox_send();
}

//User Actions

// When the select button is clicked
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Select (Refreshes)");
  //TODO Tell android to refresh app
  send_int(KEY_BUTTON_EVENT, BUTTON_REFRESH);
}

// When the up button is clicked
static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Up (Go Previous)");
  //TODO Go to previous if available
  send_int(KEY_BUTTON_EVENT, BUTTON_PREVIOUS);
}

// When the down button is clicked
static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Down (Go Next)");
  //TODO Go to next if available
  send_int(KEY_BUTTON_EVENT, BUTTON_NEXT);
}

// The Button Config
static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

// App Views

// On load of this window
static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  text_layer = text_layer_create((GRect) { .origin = { 0, 72 }, .size = { bounds.size.w, 20 } });
  text_layer_set_text(text_layer, "Press a button");
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

// On unload of this window
static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
}

// On init (App Start)
static void init(void) {
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
	.load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);
  
  //Register AppMessage events
  app_message_register_inbox_received(in_received_handler);           
  app_message_open(512, 512);    //Large input and output buffer sizes
}

// On Destroy (App Ends)
static void deinit(void) {
  window_destroy(window);
}*/

