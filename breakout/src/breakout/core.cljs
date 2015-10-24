;58
(ns breakout.core
  (:refer-clojure :exclude [update])
  (:require cljsjs.phaser
            [domina :as dom]
            [cljs.debux :refer-macros [dbg clog break]])
  (:use-macros [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n] ]))
(def state (obj :ball nil
                :padde nil
                :bricks nil
                :ball-on-paddle true
                :lives 3
                :score 0
                :score-text nil
                :lives-text nil
                :intro-text nil
                :starfield nil))

(def.n release-ball []
  (when state.ball-on-paddle
    (! state.ball-on-paddle false)
    (! state.ball.body.velocity.y -300)
    (! state.ball.body.velocity.x -75)
    (! state.ball.animations.play "spin")
    (! state.intro-text.visible false) ))

(def.n game-over []
  (state.ball.body.velocity.setTo 0 0)
  (! state.intro-text.text "Game Over!")
  (! state.intro-text.visible true))

(def.n ball-lost []
  (! state.lives (dec state.lives))
  (! state.lives-text.text (str "lives: " state.lives))

  (if (zero? state.lives)
    (game-over)
    (do
      (! state.ball-on-paddle true)
      (state.ball.reset (+ state.paddle.x 16) (- state.paddle.y 16))
      (state.ball.animations.stop) )))

(def.n ball-hit-brick
  [ball brick]
  (brick.kill)
  
  (! state.score (+ state.score 10))
  (! state.score-text.text (str "score: " state.score))

  (when (zero? state.bricks.countLiving)
    (! state.score (+ 1000 state.score))
    (! state.score-text (str "score: " state.score))
    (! state.intro-text "- Next Level -")

    (! state.ball-on-paddle true)
    (state.ball.body.velocity.set 0)
    (! state.ball.x (+ state.paddle.x 16))
    (! state.ball.y (- state.paddle.y 16))
    (state.ball.animations.stop)

    ;;  And bring the bricks back from the dead
    (state.bricks.callAll "revive") ))


(def.n ball-hit-paddle [ball paddle]
  (let [diff (- paddle.x ball.x)]
    (cond
      (zero? diff)
      (! ball.body.velocity.x (+ 2 (* 8 (Math/random))))
      
      :else
      (! ball.body.velocity.x (* 10 diff)) )))
       
;; (defn init-game []
;;   (dom/destroy-children! (dom/by-id "phaser-example"))
;;   (js/Phaser.Game. 800 600 js/Phaser.AUTO  "phaser-example"
;;                    (obj :preload #'preload :create #'create :update update)))


(def.n preload [game]
  (game.load.atlas "breakout" "assets/games/breakout/breakout.png" "assets/games/breakout/breakout.json")
  (game.load.image "starfield" "assets/misc/starfield.jpg"))

(def.n create [game]
  (game.physics.startSystem js/Phaser.Physics.ARCADE)
  (! game.physics.arcade.checkCollision.down false)

  ;; 배경 화면 추가
  (! state.starfield (game.add.tileSprite 0 0 800 600 "starfield"))

  ;; 벽돌 속성 지정
  (! state.bricks (game.add.group))
  (! state.bricks.enableBody true)
  (! state.bricks.physicsBodyType js/Phaser.Physics.ARCADE)

  ;; 벽돌 스프라이트 추가 및 속성 지정 
  (dotimes [y 4]
    (dotimes [x 15]
      (let [brick (state.bricks.create (+ 120 (* x 36))
                                       (+ 100 (* y 52))
                                       "breakout"
                                       (str "brick_" (inc y) "_1.png") )]
        (brick.body.bounce.set 1)
        (! brick.body.immovable true) )))

  ;; paddle 처리
  (! state.paddle (game.add.sprite game.world.centerX 500 "breakout" "paddle_big.png"))
  (state.paddle.anchor.setTo 0.5 0.5)
  
  (game.physics.enable state.paddle js/Phaser.Physics.ARCADE)

  (! state.paddle.body.collideWorldBounds true)
  (state.paddle.body.bounce.set 1)
  (! state.paddle.body.immovable true)

  ;; ball 처리
  (! state.ball (game.add.sprite game.world.centerX (- state.paddle.y 16)
                                 "breakout" "ball_1.png"))
  (state.ball.anchor.set 0.5)
  (! state.ball.checkWorldBounds true)

  (game.physics.enable state.ball js/Phaser.Physics.ARCADE)

  (! state.ball.body.collideWorldBounds true)
  (state.ball.body.bounce.set 1)

  (state.ball.animations.add "spin" (arr "ball_1.png" "ball_2.png" "ball_3.png" "ball_4.png" "ball_5.png")
                             50 true false)
  (state.ball.events.onOutOfBounds.add ball-lost this)

  (! state.score-text (game.add.text 32 550 "score: 0"
                                     (obj :font "20px Arial" :fill "#ffffff" :align "left") ))
  (! state.lives-text (game.add.text 680 550 "lives: 3"
                                     (obj :font "20px Arial" :fill "#ffffff" :align "left") ))
  (! state.intro-text (game.add.text game.world.centerX 400 "- click to start -"
                                     (obj :font "40px Arial" :fill "#ffffff" :align "center") ))
  (state.intro-text.anchor.setTo 0.5 0.5)

  (game.input.onDown.add release-ball, this))

(def.n update [game]
  (! state.paddle.x game.input.x)

  (let [x state.paddle.x]
    ;; paddle이 좌/우 벽에 닿는 경우 처리
    (cond
      (< x 24)
      (! state.paddle.x 24)

      (> x (- game.widdth 24))
      (! state.paddle.x (- game.width 24)))

    (if state.ball-on-paddle
      (! state.ball.body.x state.paddle.x)
      (do
        (game.physics.arcade.collide state.ball state.paddle ball-hit-paddle nil this)
        (game.physics.arcade.collide state.ball state.bricks ball-hit-brick nil this) )))
  ) 

(defn init-game []
  (dom/destroy-children! (dom/by-id "phaser-example"))
  (js/Phaser.Game. 800 600 js/Phaser.AUTO  "phaser-example"
                   (obj :preload #'preload :create #'create :update update)))

(init-game) 

(defn on-js-reload 
  []
  (clog "reloaded")) 


;; (def o (obj :a {:b {:c   10
;;                     :add (fn [x y] (+ x y)) }}))

;; (clog (? o.a.b.c))
;; (clog (! o.a.b.c 20))
;; (clog (?> o.a.b.add 100 200))

;; (clog (.call (aget o "a" "b" "add") (aget o "a" "b") 100 200))
