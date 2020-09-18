# neural-network

WARNING: not working yet!


# kotlin idiocracies


the good
*  {} is for lambdas, not a block delimiter, so {xx}; is just an expression and xx is not evaluated. using {xx}() works

the bad 
*  ?[ does not exist, i am using ?.get()
*  for .. until is exclusive but downTo is inclusive
*  main func is called from cmd line using Kt suffixed to the file name
