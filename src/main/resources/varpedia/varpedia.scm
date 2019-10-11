;Why can't you agree on whether "(tts_file "-")" means (tts_file "-") or (tts_file -)
(load (path-append (if (symbol-bound? 'datadir) datadir libdir) "init.scm"))
(gc-status nil)
(defvar chunk 0)

(define (varpedia_error message)
  (format stderr "%s: %s\n" "varpedia" message)
  (quit))

(let ((o argv))
  (while o
    (begin
	  (cond
	    ((string-equal "-o" (car o))
	      (if (not (cdr o))
	        (varpedia_error "no output file specified"))
	        (set! outfile (car (cdr o))))
	    ((string-equal "-voice" (car o))
	      (if (not (cdr o))
	        (varpedia_error "no voice specified"))
	        (eval (read-from-string (string-append "(voice_" (car (cdr o)) ")")))))
	  (set! o (cdr o))
	  (set! o (cdr o)))))

(if (boundp 'outfile)
  (set! tts_hooks (list utt.synth (lambda (x) (utt.save.wave x (string-append outfile "/" chunk ".wav")) (set! chunk (+ chunk 1))))))

(tts_file "-")