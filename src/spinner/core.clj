
(ns spinner.core
  (:require [clojure.core.async :as a]
            [clojure.string :as str]
            [com.bunimo.clansi :refer [style ansi]]
            [clojure.tools.cli :refer [parse-opts]]))

(def spinners {
               :strokes { :interval 130
                         :frames [ "|", "/", "\u2014", "\\"]}
               :balls { :interval 140
                       :frames  [ "◓","◑", "◒", "◐"]}
               :dqpb { :interval 100
                       :frames  [ "d", "q", "p", "b"]}
               :braille { :frames  [ "⠋" "⠙" "⠹" "⠸" "⠼" "⠴" "⠦" "⠧" "⠇" "⠏" ]} ;  Stolen from henrik/progress_bar & WebTranslateI
               :kit { :interval 120 :frames [ "▉", "▊", "▋", "▌", "▍", "▎", "▏", "▎", "▍", "▌", "▋", "▊", "▉" ]}
               :lpg { :frames [ "▖" "▘" "▝" "▗" "▖" "▌" "▛" "█" "▜" "▐" "▗" "\u2002" ]}
               :tone-bar { :frames [ "꜍" "꜎" "꜏" "꜐" "꜑" "꜌" "꜋" "꜊" "꜉" "꜈" ]}
               :clock {:interval 100 :frames [ "🕐" "🕑" "🕒" "🕓" "🕔" "🕕" "🕖" "🕗" "🕘" "🕙" "🕚" "🕛" ]}
               :more-clock {:interval 500
                            :frames ["🕐" "🕜" "🕑" "🕝" "🕒" "🕞" "🕓" "🕟" "🕔" "🕠" "🕕" "🕡" "🕖" "🕢" "🕗" "🕣" "🕘" "🕤" "🕙" "🕥" "🕚" "🕦" "🕛" "🕧" ]}
               :moon { :frames [ "🌑" "🌒" "🌓" "🌔" "🌕" "🌖" "🌗" "🌘" ]}
               :grow-vertical {:interval 120
                               :frames ["\u2581" "\u2582" "\u2583" "\u2584" "\u2585" "\u2586" "\u2587" "\u2588"
                                        "\u2587" "\u2586" "\u2585" "\u2584" "\u2583" "\u2582" "\u2581" "\u2002" ]}
               :grow-horizontal {:interval 120
                                 :frames ["\u258f" "\u258e" "\u258d" "\u258c" "\u258b" "\u258a" "\u2589" "\u2588" "\u2588" "\u2588"
                                          "\u2589" "\u258a" "\u258b" "\u258c" "\u258d" "\u258e" "\u258f" "\u2002" "\u2002"]}
               :balloon {:interval 140
                         :frames [" ", ".", "o", "O", "@", "*", " "]}

               :pipe { :frames [  "┤", "┘", "┴", "└", "├", "┌", "┬", "┐" ]}
               :flip {:interval 70
                      :frames ["_", "_", "_", "-", "`", "`", "'", "´", "-", "_", "_", "_"]}
               :star {:interfal 70
                      :frames ["✶", "✸", "✹", "✺", "✹", "✷"]}
               :star2 {:interval 80 :frames ["+", "x", "*"]}

               :triangle {:interval 50 :frames ["◢", "◣", "◤", "◥"]}

               :hamburger { :frames [ "☱", "☲", "☴", "☲"]}
               :simple-dots {:interval 400 :frames [".  ", ".. ", "...", "   "]}
               :simple-dots-scrolling {:interval 200 :frames [ ".  ", ".. ", "...", " ..", "  .", " ..", "...", ".. " ]}

               :dots {:interval 80
                      :frames ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" ]}
               :dots02 {:interval 80 :frames ["⣾", "⣽", "⣻", "⢿", "⡿", "⣟", "⣯", "⣷" ]}
               :dots03 {:interval 80 :frames ["⠋", "⠙", "⠚", "⠞", "⠖", "⠦", "⠴", "⠲", "⠳", "⠓" ]}
               :dots04 {:interval 80 :frames ["⠄", "⠆", "⠇", "⠋", "⠙", "⠸", "⠰", "⠠", "⠰", "⠸", "⠙", "⠋", "⠇", "⠆" ]}
               :dots05 {:interval 80
                       :frames ["⠋", "⠙", "⠚", "⠒", "⠂", "⠂", "⠒", "⠲", "⠴", "⠦", "⠖", "⠒", "⠐", "⠐", "⠒", "⠓", "⠋"]}
               :dots06 {:interval 80
                       :frames ["⠁", "⠉", "⠙", "⠚", "⠒", "⠂", "⠂", "⠒", "⠲", "⠴", "⠤", "⠄", "⠄", "⠤", "⠴", "⠲", "⠒", "⠂", "⠂", "⠒", "⠚", "⠙", "⠉", "⠁"]}
               :dots07 {:interval 80
                       :frames ["⠈", "⠉", "⠋", "⠓", "⠒", "⠐", "⠐", "⠒", "⠖", "⠦", "⠤", "⠠", "⠠", "⠤", "⠦", "⠖", "⠒",
                                "⠐", "⠐", "⠒", "⠓", "⠋", "⠉", "⠈"]}
               :dots08 {:interval 80
                       :frames ["⠁", "⠁", "⠉", "⠙", "⠚", "⠒", "⠂", "⠂", "⠒", "⠲", "⠴", "⠤", "⠄", "⠄", "⠤", "⠠", "⠠",
                                "⠤", "⠦", "⠖", "⠒", "⠐", "⠐", "⠒", "⠓", "⠋", "⠉", "⠈", "⠈"]}
               :dots09 {:interval 80
                       :frames ["⢹", "⢺", "⢼", "⣸", "⣇", "⡧", "⡗", "⡏"]}
               :dots10 {:interval 80
                       :frames ["⢄", "⢂", "⢁", "⡁", "⡈", "⡐", "⡠"]}
               :dots11 {:interval 100
                       :frames ["⠁", "⠂", "⠄", "⡀", "⢀", "⠠", "⠐", "⠈"]}
               :dots12 {:interval 80
                        :frames ["⢀⠀", "⡀⠀", "⠄⠀", "⢂⠀", "⡂⠀", "⠅⠀", "⢃⠀", "⡃⠀",
                                 "⠍⠀", "⢋⠀", "⡋⠀", "⠍⠁", "⢋⠁", "⡋⠁", "⠍⠉", "⠋⠉",
                                 "⠋⠉", "⠉⠙", "⠉⠙", "⠉⠩", "⠈⢙", "⠈⡙", "⢈⠩", "⡀⢙",
                                 "⠄⡙", "⢂⠩", "⡂⢘", "⠅⡘", "⢃⠨", "⡃⢐", "⠍⡐", "⢋⠠",
                                 "⡋⢀", "⠍⡁", "⢋⠁", "⡋⠁", "⠍⠉", "⠋⠉", "⠋⠉", "⠉⠙",
                                 "⠉⠙", "⠉⠩", "⠈⢙", "⠈⡙", "⠈⠩", "⠀⢙", "⠀⡙", "⠀⠩",
                                 "⠀⢘", "⠀⡘", "⠀⠨", "⠀⢐", "⠀⡐", "⠀⠠", "⠀⢀", "⠀⡀" ]}

               :arrow { :interval 100
                       :frames [ "←", "↖", "↑", "↗", "→", "↘", "↓", "↙" ]}

               :dots-single { :frames [ "\u2801" "\u2802" "\u2804" "\u2840" "\u2880" "\u2820" "\u2810" "\u2808"]}
               :dots-triple { :frames [ "\u280b" "\u2807" "\u2846" "\u28c4" "\u28e0" "\u28b0" "\u2838" "\u2819"]}
               :dots-quad { :frames   [ "\u281b" "\u280f" "\u2847" "\u28c6" "\u28e4" "\u28f0" "\u28b8" "\u2839"]}
               :dots-bounce { :frames [ "\u2800" "\u2809" "\u2812" "\u2824" "\u28c0" "\u2800" "\u28c0" "\u2824" "\u2812" "\u2809" ]}
               :dots-strobe { :frames [ "\u2800" "\u2809" "\u281b" "\u283f" "\u28ff" "\u28f6" "\u28e4" "\u28c0" ]}
               :dots-1-snake { :frames  [ "\u2801" "\u2808" "\u2810" "\u2802"
                                          "\u2804" "\u2820" "\u2880" "\u2840"
                                          "\u2804" "\u2820" "\u2810" "\u2802" ]}
               :dots-2-snake { :frames  [ "\u2809" "\u2818" "\u2812" "\u2806"
                                          "\u2824" "\u28a0" "\u28c0" "\u2844"
                                          "\u2824" "\u2830" "\u2812" "\u2803" ]}
               :dots-box-strobe { :frames [ "\u281b" "\u2836" "\u28e4" "\u2836" ]}
               :dots-box-splat { :frames  [ "\u2809" "\u281b" "\u2836" "\u28e4" "\u28c0" "\u28e4" "\u2836" "\u281b" ]}
               :dots-fall { :frames [ "\u2809" "\u280a" "\u2818" "\u2811" "\u2812" "\u2814" "\u2830" "\u2822"
                                      "\u2824" "\u2860" "\u28a0" "\u2884" "\u28c0" "\u2800" ]}

               :test { :frames [ "[=   ]", "[ =  ]", "[  = ]", "[   =]",
                                 "[  = ]", "[ =  ]" ]}
               :bouncing-ball { :interval 80
                               :frames ["( ●    )", "(  ●   )", "(   ●  )", "(    ● )",
                                        "(     ●)", "(    ● )", "(   ●  )", "(  ●   )",
                                        "( ●    )", "(●     )" ]}
               :bouncing-bar { :interval 80
                              :frames ["[    ]", "[=   ]", "[==  ]", "[=== ]", "[ ===]", "[  ==]", "[   =]",
                                       "[    ]", "[   =]", "[  ==]", "[ ===]", "[====]", "[=== ]", "[==  ]",
                                       "[=   ]" ]}
               :monkey { :interval 300
                        :frames [ "🙈 ", "🙈 ", "🙉 ", "🙊 " ]}
               :arc { :frames [ "◜", "◠", "◝", "◞", "◡", "◟" ]}
               :layer {:interval 150
                       :frames ["-", "=", "≡", "="]}
               :noise {:interval 100 :frames ["▓", "▒", "░"]}

               :shark {:interval 120
                       :frames ["▐|\\____________▌", "▐_|\\___________▌", "▐__|\\__________▌", "▐___|\\_________▌", "▐____|\\________▌",
                                "▐_____|\\_______▌", "▐______|\\______▌", "▐_______|\\_____▌", "▐________|\\____▌", "▐_________|\\___▌",
                                "▐__________|\\__▌", "▐___________|\\_▌", "▐____________|\\▌", "▐____________/|▌", "▐___________/|_▌",
                                "▐__________/|__▌", "▐_________/|___▌", "▐________/|____▌", "▐_______/|_____▌", "▐______/|______▌",
                                "▐_____/|_______▌", "▐____/|________▌", "▐___/|_________▌", "▐__/|__________▌", "▐_/|___________▌", "▐/|____________▌"]}
               })

  ;; Braille
  ;; \u2800
  ;; \u28FF
(defn rand-braille
  "Return a sequence of random selected braille characters"
  []
  (repeatedly #(char (+ 0x2800 (rand-int 256)))))

;; playing cards
(defn rand-cards
  "Return a sequence of random selected playing card characters"
  []
  (repeatedly #(char (+ 0x1F0A0 (rand-int 256)))))

;; matix style random
;;    (doseq [it (take 400 (rand-char))]
;;      (print (str "\r" (style it :green :bright) "\u2002"))
;;      (a/<!! (a/timeout 50))
;;      (flush))

(defn rand-char []
  (repeatedly #(str (char (+ (rand 121) 34)))))

(defn clear-line []
  (print "\033[2K"))

(def cli-options
  [["-s" "--spinner spinner" "The spinner to display"
    :default "strokes"]
   ["-i" "--interval num" "milliseconeds between spinner frames"
    :parse-fn #(Integer/parseInt %)]
   ["-c" "--countdown num" "Countdown before spinning"
    :parse-fn #(Integer/parseInt %)
    :default 0]
   ["-t" "--time num" "display spinner for # seconds"
    :parse-fn #(Integer/parseInt %)]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn usage [options-summary]
  (str/join \newline
            [(str (style "spin" :bold :white) " A command line tool to show spinning graphics")
             ""
             (style "Options:" :bold :cyan)
             options-summary
             ""]
            ))

(defn error-msg [errors]
  (str "Trouble parsing command line arguments:\n\n"
       (str/join \newline errors)))

(defn exit 
  ([status]
    (System/exit status))
  ([status msg]
    (println msg)
    (System/exit status)))

(defn -main [& args]
  (let [c (a/chan)
        begin (System/currentTimeMillis)
        {:keys [options arguments errors summary]} (parse-opts args cli-options :in-order true)
        spinner (:spinner options)
        interval (:interval options)
        countdown (:countdown options)
        duration (:time options)]

    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))

    (doseq [n (range countdown 0 -1)]
      (print (str (if (= n countdown) "" "\r") "Starting in " n))
      (flush)
      (a/<!! (a/timeout 1000)))

    (clear-line)
    (flush)

    (when (nil? (spinners (keyword spinner)))
      (println (str (style "The spinner '" :green) (style spinner :bold) (style "' not found." :green)))
      (println (style "Available spinners:"  :bold :green))
      (doseq [sp (sort (keys spinners))]
        (println (style  (str "  " (name sp)) :yellow)))
      (exit 1))

    (let [spinner-frames (:frames (spinners (keyword spinner)))
          spinner-interval (or interval (:interval (spinners (keyword spinner))) 333)
          spinner-duration (or (and duration (* duration 1000)) 4000)
          cycle-times  (or (and (not= 0 spinner-interval) (/ spinner-duration spinner-interval)) 40)]
      (doseq [glyph (take cycle-times (cycle spinner-frames))]
        ;; (clear-line)
        (print (str "\r" glyph " "))
        (flush)
        (a/<!! (a/timeout spinner-interval))))

    (clear-line)
    (println (str "\r" (style "\u2714" :green) " Done."))))
  
