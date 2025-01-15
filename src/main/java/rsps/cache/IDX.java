package rsps.cache;

/**
 * The IDX (Index) enumeration represents the various data types stored in the RuneScape cache.
 * RuneScape's caches are data storage systems used by the game to efficiently store and retrieve game-related assets.
 * Each IDX enum constant corresponds to a specific data type or category within the cache.
 *
 * @author Albert Beaupre
 */
public enum IDX {
    // Index constants for different data types in the RuneScape cache

    ANIMATION_SKELETONS(0),       // Skeleton animations used for character movement
    ANIMATION_BASES(1),           // Base animations for characters
    GRAPHIC_ACCESSORIES(2),       // Graphic accessories used for visual effects
    INTERFACES(3),                // Game interfaces and UI components
    SOUND_EFFECTS(4),             // Sound effects used in the game
    LANDSCAPES(5),                // Landscape data for in-game environments
    MUSIC(6),                     // Music tracks used in various game situations
    MODELS(7),                    // 3D models of in-game objects and characters
    SPRITES(8),                   // 2D sprites and images
    TEXTURES(9),                  // Textures used for rendering surfaces
    HUFFMAN(10),                  // Huffman encoding data for efficient compression
    MUSIC2(11),                   // Additional music tracks
    INTERFACE_SCRIPTS(12),        // Scripts associated with game interfaces
    FONT_METRICS(13),             // Metrics and parameters for font rendering
    SOUND_EFFECTS2(14),           // Additional sound effects
    SOUND_EFFECTS3(15),           // More sound effects
    OBJECTS(16),                  // In-game objects and their properties
    ENUMS(17),                    // Enumerations used in the game
    NPCS(18),                     // Non-player characters (NPCs) and their properties
    ITEMS(19),                    // Items and their attributes
    ANIMATIONS(20),               // Additional animations
    GRAPHICS(21),                 // Additional graphics and visual assets
    CONFIGURATION(22),            // Game configuration settings
    WORLD_MAP(23),                // Data related to the in-game world map
    QUICK_CHAT_MESSAGES(24),      // Quick chat messages and their translations
    QUICK_CHAT_MENU(25),          // Quick chat menu options
    TEXTURE_CONFIG(26),           // Configuration settings for textures
    PARTICLES(27),                // Particle effects used in the game
    DEFAULTS(28),                 // Default data and configurations
    BILLBOARDS(29),               // Billboards and advertising content
    NATIVE_LIBRARIES(30),         // Native libraries and dependencies
    SHADERS(31),                  // Shader programs used for graphical rendering
    LOADING_FONT_IMAGES(32),      // Font images used during loading screens
    GAME_TIPS(33),                // Tips and hints displayed in the game
    LOADING_FONT_IMAGES2(34),     // Additional font images for loading screens
    THEORA(35),                   // Video codec data for Theora video playback
    VORBIS(36),                   // Audio codec data for Vorbis audio playback
    META(255);                    // Meta-data and miscellaneous information

    private final int index;       // The numeric index associated with each IDX constant

    /**
     * Constructor for the IDX enum.
     *
     * @param index The numeric index associated with the IDX constant.
     */
    IDX(int index) {
        this.index = index;
    }

    /**
     * Retrieves the IDX constant associated with a specific numeric index.
     *
     * @param index The numeric index to search for.
     * @return The corresponding IDX constant, or null if no match is found.
     */
    public static IDX of(int index) {
		if (index == 225)
			return META;
        return values()[index];
    }

    /**
     * Retrieves the numeric index associated with the IDX constant.
     *
     * @return The numeric index.
     */
    public int index() {
        return index;
    }
}