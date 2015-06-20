#include <pebble.h>
#include "bus_layout.h"
  
// Main Loop
int main(void) {
  show_bus_layout();
  app_event_loop();
  hide_bus_layout();
  return 0;
}
