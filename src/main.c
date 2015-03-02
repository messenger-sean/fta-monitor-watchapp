#include <pebble.h>
  
// Constant text strings for the connection statuses
const char *eth = "Eth", *ds = "DS", *radio = "Rd", *rio = "RIO", *code = "Cd", *estop = "Est", *good = "G";
  
static Window *s_main_window;
static Layer *s_grid_layer;
static TextLayer *s_red_header;
static TextLayer *s_blue_header;
static TextLayer *s_red1;
static TextLayer *s_red2;
static TextLayer *s_red3;
static TextLayer *s_blue1;
static TextLayer *s_blue2;
static TextLayer *s_blue3;

static void canvas_update_proc(Layer *this_layer, GContext *ctx) {
  // Draw a line down the middle of the screen
  graphics_draw_line(ctx, GPoint(74, 0), GPoint(74, 168));
  
  // Draw the alliance line
  graphics_draw_line(ctx, GPoint(0, 20), GPoint(144, 20));
  
  // Draw the 2 actual alliance dividers. They are (168-20-10)/3 pixels apart, which is 46.
  graphics_draw_line(ctx, GPoint(0, 66), GPoint(144, 66));
  graphics_draw_line(ctx, GPoint(0, 112), GPoint(144, 112));
}

void setup_alliance_textlayer(TextLayer **layer, Layer *parent, const char *text, int x, int y) {
  *layer = text_layer_create(GRect(x, y, 74, 46));
  text_layer_set_background_color(*layer, GColorClear);
  text_layer_set_text_color(*layer, GColorBlack);
  text_layer_set_text_alignment(*layer, GTextAlignmentCenter);
  text_layer_set_font(*layer, fonts_get_system_font(FONT_KEY_BITHAM_42_LIGHT));
  text_layer_set_text(*layer, text);
  layer_add_child(parent, text_layer_get_layer(*layer));
}

static void main_window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);
  
  // Create the grid layer
  s_grid_layer = layer_create(GRect(0, 0, bounds.size.w, bounds.size.h));
  layer_add_child(window_layer, s_grid_layer);
  
  // Set the grid drawing update procedure
  layer_set_update_proc(s_grid_layer, canvas_update_proc);
  
  // Draw the Red and Blue text for the alliance headers
  s_red_header = text_layer_create(GRect(0, 0, 74, 20));
  s_blue_header = text_layer_create(GRect(74, 0, 74, 20));
  text_layer_set_background_color(s_red_header, GColorClear);
  text_layer_set_background_color(s_blue_header, GColorClear);
  text_layer_set_text_color(s_red_header, GColorBlack);
  text_layer_set_text_color(s_blue_header, GColorBlack);
  text_layer_set_text_alignment(s_red_header, GTextAlignmentCenter);
  text_layer_set_text_alignment(s_blue_header, GTextAlignmentCenter);
  text_layer_set_font(s_red_header, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_font(s_blue_header, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_text(s_red_header, "Red");
  text_layer_set_text(s_blue_header, "Blue");
  layer_add_child(window_layer, text_layer_get_layer(s_red_header));
  layer_add_child(window_layer, text_layer_get_layer(s_blue_header));
  
  // Create the layers for the different alliance statuses
  setup_alliance_textlayer(&s_red1, window_layer, eth, 0, 20);
  setup_alliance_textlayer(&s_red2, window_layer, ds, 0, 66);
  setup_alliance_textlayer(&s_red3, window_layer, radio, 0, 112);
  setup_alliance_textlayer(&s_blue1, window_layer, rio, 74, 20);
  setup_alliance_textlayer(&s_blue2, window_layer, code, 74, 66);
  setup_alliance_textlayer(&s_blue3, window_layer, estop, 74, 112);
  text_layer_set_background_color(s_blue3, GColorBlack);
  text_layer_set_text_color(s_blue3, GColorWhite);
}

static void main_window_unload(Window *window) {
  layer_destroy(s_grid_layer);
  text_layer_destroy(s_red_header);
  text_layer_destroy(s_blue_header);
  text_layer_destroy(s_red1);
  text_layer_destroy(s_red2);
  text_layer_destroy(s_red3);
  text_layer_destroy(s_blue1);
  text_layer_destroy(s_blue2);
  text_layer_destroy(s_blue3);
}

static void init() {
  s_main_window = window_create();
  window_set_window_handlers(s_main_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload
  });
  
  window_stack_push(s_main_window, true);
}

static void deinit() {
  window_destroy(s_main_window);
}

int main() {
  init();
  app_event_loop();
  deinit();
}